package com.instream.tenant.domain.application.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.model.specification.ApplicationSessionSpecification;
import com.instream.tenant.domain.application.model.specification.ApplicationDynamicQueryBuilder;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.domain.dto.*;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ApplicationService {
    private final ReactiveRedisTemplate<String, ApplicationEntity> redisTemplate;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    private final ApplicationDynamicQueryBuilder applicationDynamicQueryBuilder;

    @Autowired
    public ApplicationService(ReactiveRedisTemplateFactory redisTemplateFactory, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantJoinRepository participantJoinRepository, ApplicationDynamicQueryBuilder applicationDynamicQueryBuilder) {
        this.redisTemplate = redisTemplateFactory.getTemplate(ApplicationEntity.class);
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantJoinRepository = participantJoinRepository;
        this.applicationDynamicQueryBuilder = applicationDynamicQueryBuilder;
    }

    public Mono<PaginationDto<CollectionDto<ApplicationWithApiKeyDto>>> search(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
        Pageable pageable = applicationSearchPaginationOptionRequest.getPageable();
        Predicate predicate = applicationDynamicQueryBuilder.getPredicate(applicationSearchPaginationOptionRequest, hostId);
        List<OrderSpecifier> orderSpecifier = applicationDynamicQueryBuilder.getOrderSpecifier(applicationSearchPaginationOptionRequest);

        Flux<ApplicationEntity> applicationFlux = applicationRepository.query(sqlQuery -> sqlQuery
                .select(QApplicationEntity.applicationEntity)
                .from(QApplicationEntity.applicationEntity)
                .where(predicate)
                .orderBy(orderSpecifier.toArray(new OrderSpecifier[0]))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();
        Flux<ApplicationWithApiKeyDto> applicationDtoFlux = applicationFlux
                .flatMap(application -> applicationSessionRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                        .flatMap(applicationSession -> Mono.just(ApplicationWithApiKeyDto.builder()
                                .applicationId(application.getId())
                                .apiKey(application.getApiKey())
                                .session(ApplicationSessionDto.builder()
                                        .id(applicationSession.getId())
                                        .createdAt(applicationSession.getCreatedAt())
                                        .deletedAt(applicationSession.getDeletedAt())
                                        .build())
                                .type(application.getType())
                                .status(application.getStatus())
                                .createdAt(application.getCreatedAt())
                                .build()))
                        .defaultIfEmpty(ApplicationWithApiKeyDto.builder()
                                .applicationId(application.getId())
                                .apiKey(application.getApiKey())
                                .type(application.getType())
                                .status(application.getStatus())
                                .createdAt(application.getCreatedAt())
                                .build()
                        ));

        // TODO: Redis 캐싱 넣기
        if (applicationSearchPaginationOptionRequest.isFirstView()) {
            return applicationDtoFlux.collectList()
                    .flatMap(applicationDtoList -> applicationRepository.count(predicate)
                            .flatMap(count -> {
                                int totalElementCount = (int) Math.ceil((double) count / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<ApplicationWithApiKeyDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(count)
                                        .currentPage(applicationSearchPaginationOptionRequest.getPage())
                                        .data(CollectionDto.<ApplicationWithApiKeyDto>builder()
                                                .data(applicationDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return applicationDtoFlux.collectList()
                .flatMap(applicationDtoList -> Mono.just(PaginationDto.<CollectionDto<ApplicationWithApiKeyDto>>builder()
                        .currentPage(applicationSearchPaginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<ApplicationWithApiKeyDto>builder()
                                .data(applicationDtoList)
                                .build())
                        .build()));
    }

    public Mono<ApplicationWithApiKeyDto> createApplication(ApplicationCreateRequest applicationCreateRequest, UUID hostId) {
        String apiKey = UUID.randomUUID().toString();

        return Mono.defer(() -> applicationRepository.save(
                        ApplicationEntity.builder()
                                .tenantId(hostId)
                                .apiKey(apiKey)
                                .type(applicationCreateRequest.type())
                                .status(Status.PENDING)
                                .build()
                ))
                .flatMap(application -> redisTemplate.opsForValue()
                        .set(String.valueOf(application.genRedisKey()), application)
                        .thenReturn(application))
                .flatMap(savedApplication -> Mono.just(ApplicationWithApiKeyDto.builder()
                        .applicationId(savedApplication.getId())
                        .type(savedApplication.getType())
                        .status(savedApplication.getStatus())
                        .createdAt(savedApplication.getCreatedAt())
                        .apiKey(apiKey)
                        .build()
                ));
    }

    public Mono<ApplicationDto> startApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean invalidStatus = application.getStatus() != Status.PENDING;

                    if (invalidStatus) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_CAN_NOT_MODIFY));
                    }

                    application.setStatus(Status.USE);
                    return applicationRepository.save(application).thenReturn(application);
                })
                .flatMap(applicationEntity -> Mono.just(ApplicationDto.builder()
                        .applicationId(applicationEntity.getId())
                        .status(applicationEntity.getStatus())
                        .type(applicationEntity.getType())
                        .createdAt(applicationEntity.getCreatedAt())
                        .build())
                );
    }

    public Mono<ApplicationDto> endApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean isOn = application.getStatus() != Status.USE;

                    if (isOn) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_CAN_NOT_MODIFY));
                    }

                    application.setStatus(Status.USE);
                    return applicationRepository.save(application).thenReturn(application);
                })
                .flatMap(applicationEntity -> Mono.just(ApplicationDto.builder()
                        .applicationId(applicationEntity.getId())
                        .status(applicationEntity.getStatus())
                        .type(applicationEntity.getType())
                        .createdAt(applicationEntity.getCreatedAt())
                        .build())
                );
    }

    public Mono<Void> deleteApplication(String apiKey, UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    if (!Objects.equals(application.getApiKey(), apiKey)) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
                    }
                    return applicationSessionRepository.deleteByApplicationId(applicationId)
                            .then(Mono.defer(() -> applicationRepository.deleteById(applicationId)));
                });
    }

    public Mono<PaginationDto<CollectionDto<ApplicationSessionDto>>> searchSessions(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest, UUID hostId, UUID applicationId) {
        // TODO: 호스트 인증 넣기

        Pageable pageable = applicationSessionSearchPaginationOptionRequest.getPageable();
        Predicate predicate = ApplicationSessionSpecification.with(applicationSessionSearchPaginationOptionRequest, applicationId);

        // TODO: 체인 하나로 묶기
        Flux<ApplicationSessionEntity> applicationSessionFlux = applicationSessionRepository.query(sqlQuery -> sqlQuery
                .select(QApplicationSessionEntity.applicationSessionEntity)
                .from(QApplicationSessionEntity.applicationSessionEntity)
                .where(predicate)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();

        Mono<List<ApplicationSessionDto>> applicationSessionDtoListMono = applicationSessionFlux
                .map(applicationSessionEntity -> ApplicationSessionDto.builder()
                        .id(applicationSessionEntity.getId())
                        .createdAt(applicationSessionEntity.getCreatedAt())
                        .deletedAt(applicationSessionEntity.getDeletedAt())
                        .build())
                .collectList();

        // TODO: Redis 캐싱 넣기
        if (applicationSessionSearchPaginationOptionRequest.isFirstView()) {
            Mono<Long> totalElementCountMono = applicationSessionRepository.count(predicate);
            Mono<Integer> totalPageCountMono = totalElementCountMono.map(count -> (int) Math.ceil((double) count / pageable.getPageSize()));

            return Mono.zip(applicationSessionDtoListMono, totalPageCountMono, totalElementCountMono)
                    .map(tuple -> {
                        List<ApplicationSessionDto> applicationSessionDtoList = tuple.getT1();
                        Integer pageCount = tuple.getT2();
                        int totalElementCount = tuple.getT3().intValue();

                        return PaginationInfoDto.<CollectionDto<ApplicationSessionDto>>builder()
                                .totalElementCount(totalElementCount)
                                .pageCount(pageCount)
                                .currentPage(applicationSessionSearchPaginationOptionRequest.getPageable().getPageNumber())
                                .data(CollectionDto.<ApplicationSessionDto>builder()
                                        .data(applicationSessionDtoList)
                                        .build())
                                .build();
                    });
        }

        return applicationSessionDtoListMono.map(applicationDtoList -> PaginationDto.<CollectionDto<ApplicationSessionDto>>builder()
                .currentPage(applicationSessionSearchPaginationOptionRequest.getPageable().getPageNumber())
                .data(CollectionDto.<ApplicationSessionDto>builder()
                        .data(applicationDtoList)
                        .build())
                .build()
        );
    }

    public Mono<UUID> startApplicationSession(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    if (application.getStatus() != Status.USE) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
                    }

                    ApplicationSessionEntity applicationSessionEntity = ApplicationSessionEntity.builder()
                            .applicationId(applicationId)
                            .build();
                    return applicationSessionRepository.save(applicationSessionEntity)
                            .then(applicationSessionRepository.findById(applicationSessionEntity.getId()))
                            .flatMap(retrievedApplicationSession -> Mono.just(retrievedApplicationSession.getId()));
                });
    }

    public Mono<UUID> endApplicationSession(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    if (application.getStatus() != Status.USE) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
                    }
                    return Mono.just(application);
                })
                .thenMany(applicationSessionRepository.findByApplicationIdAndDeletedAtIsNull(applicationId))
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> {
                    applicationSession.setDeletedAt(LocalDateTime.now());
                    return applicationSessionRepository.save(applicationSession);
                })
                .flatMap(applicationSession -> participantJoinRepository
                        .updateAllParticipantJoinsBySessionId(applicationSession.getId())
                        .thenReturn(applicationSession)
                )
                .sort((session1, session2) -> session2.getCreatedAt().compareTo(session1.getCreatedAt())) // createdAt으로 정렬
                .next()
                .map(ApplicationSessionEntity::getId);
    }
}
