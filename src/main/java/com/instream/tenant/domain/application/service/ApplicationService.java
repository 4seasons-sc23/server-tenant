package com.instream.tenant.domain.application.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.application.model.specification.ApplicationSessionSpecification;
import com.instream.tenant.domain.application.model.specification.ApplicationSpecification;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    public ApplicationService(ReactiveRedisTemplateFactory redisTemplateFactory, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantJoinRepository participantJoinRepository) {
        this.redisTemplate = redisTemplateFactory.getTemplate(ApplicationEntity.class);
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantJoinRepository = participantJoinRepository;
    }

    public Mono<PaginationDto<CollectionDto<ApplicationDto>>> search(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
        Pageable pageable = applicationSearchPaginationOptionRequest.getPageable();
        Predicate predicate = ApplicationSpecification.with(applicationSearchPaginationOptionRequest);

        Flux<ApplicationEntity> applicationFlux = applicationRepository.query(sqlQuery -> sqlQuery
                .select(QApplicationEntity.applicationEntity)
                .from(QApplicationEntity.applicationEntity)
                .where(predicate)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();
        Mono<List<ApplicationDto>> applicationDtoListMono = applicationFlux
                .flatMap(applicationEntity -> applicationSessionRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationEntity.getId())
                        .map(applicationSessionEntity -> ApplicationDto.builder()
                                .applicationId(applicationEntity.getId())
                                .apiKey(applicationEntity.getApiKey())
                                .session(ApplicationSessionDto.builder()
                                        .id(applicationSessionEntity.getId())
                                        .createdAt(applicationSessionEntity.getCreatedAt())
                                        .deletedAt(applicationSessionEntity.getDeletedAt())
                                        .build())
                                .type(applicationEntity.getType())
                                .status(applicationEntity.getStatus())
                                .createdAt(applicationEntity.getCreatedAt())
                                .build())
                        .defaultIfEmpty(ApplicationDto.builder()
                                .applicationId(applicationEntity.getId())
                                .type(applicationEntity.getType())
                                .status(applicationEntity.getStatus())
                                .createdAt(applicationEntity.getCreatedAt())
                                .build())
                )
                .collectList();

        // TODO: Redis 캐싱 넣기
        if (applicationSearchPaginationOptionRequest.isFirstView()) {
            Mono<Long> totalElementCountMono = applicationRepository.count(predicate);
            Mono<Integer> totalPageCountMono = totalElementCountMono.map(count -> (int) Math.ceil((double) count / pageable.getPageSize()));

            return Mono.zip(applicationDtoListMono, totalPageCountMono, totalElementCountMono)
                    .map(tuple -> {
                        List<ApplicationDto> applications = tuple.getT1();
                        Integer pageCount = tuple.getT2();
                        int totalElementCount = tuple.getT3().intValue();

                        return PaginationInfoDto.<CollectionDto<ApplicationDto>>builder()
                                .totalElementCount(totalElementCount)
                                .pageCount(pageCount)
                                .currentPage(applicationSearchPaginationOptionRequest.getPage())
                                .data(CollectionDto.<ApplicationDto>builder()
                                        .data(applications)
                                        .build())
                                .build();
                    });
        }

        return applicationDtoListMono.map(applicationDtoList -> PaginationDto.<CollectionDto<ApplicationDto>>builder()
                .currentPage(applicationSearchPaginationOptionRequest.getPage())
                .data(CollectionDto.<ApplicationDto>builder()
                        .data(applicationDtoList)
                        .build())
                .build()
        );
    }

    public Mono<ApplicationDto> createApplication(ApplicationCreateRequest applicationCreateRequest, UUID hostId) {
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
                .flatMap(savedApplication -> Mono.just(ApplicationDto.builder()
                        .applicationId(savedApplication.getId())
                        .type(savedApplication.getType())
                        .status(savedApplication.getStatus())
                        .createdAt(savedApplication.getCreatedAt())
                        .apiKey(apiKey)
                        .build()
                ));
    }

    public Mono<UUID> startApplication(String apiKey, UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean isOff = application.getStatus() == Status.PENDING;

                    // TODO: ApiKey 암호화 로직 추가하기
                    if (!Objects.equals(application.getApiKey(), apiKey)) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
                    }
                    if (isOff) {
                        return createApplicationSession(application);
                    }

                    return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                });
    }

    public Mono<UUID> endApplication(String apiKey, UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean isOn = application.getStatus() == Status.USE;

                    if (!Objects.equals(application.getApiKey(), apiKey)) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
                    }
                    if (isOn) {
                        return deleteApplicationSession(application);
                    }

                    return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                });
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
                                .currentPage(applicationSessionSearchPaginationOptionRequest.getPage())
                                .data(CollectionDto.<ApplicationSessionDto>builder()
                                        .data(applicationSessionDtoList)
                                        .build())
                                .build();
                    });
        }

        return applicationSessionDtoListMono.map(applicationDtoList -> PaginationDto.<CollectionDto<ApplicationSessionDto>>builder()
                .currentPage(applicationSessionSearchPaginationOptionRequest.getPage())
                .data(CollectionDto.<ApplicationSessionDto>builder()
                        .data(applicationDtoList)
                        .build())
                .build()
        );
    }

    private Mono<UUID> createApplicationSession(ApplicationEntity application) {
        ApplicationSessionEntity applicationSessionEntity = ApplicationSessionEntity.builder()
                .applicationId(application.getId())
                .build();

        return applicationSessionRepository.save(applicationSessionEntity)
                .flatMap(applicationSession -> {
                    application.setStatus(Status.USE);
                    return applicationRepository.save(application).then(applicationSessionRepository.findById(applicationSession.getId()));
                })
                .flatMap(retrievedApplicationSession -> Mono.just(retrievedApplicationSession.getId()));
    }

    private Mono<UUID> deleteApplicationSession(ApplicationEntity application) {
        return applicationSessionRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> {
                    applicationSession.setDeletedAt(LocalDateTime.now());
                    return applicationSessionRepository.save(applicationSession);
                })
                .flatMap(applicationSession -> participantJoinRepository.updateAllParticipantJoinsBySessionId(applicationSession.getId())
                        .then(Mono.defer(() -> {
                            application.setStatus(Status.PENDING);
                            return applicationRepository.save(application);
                        }))
                        .thenReturn(applicationSession.getId()));
    }
}
