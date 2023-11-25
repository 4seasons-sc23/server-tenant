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
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
import com.instream.tenant.domain.application.infra.mapper.ApplicationSessionMapper;
import com.instream.tenant.domain.application.model.specification.ApplicationSessionQueryBuilder;
import com.instream.tenant.domain.application.model.specification.ApplicationQueryBuilder;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.domain.dto.*;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.media.domain.request.NginxRtmpRequest;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class ApplicationService {
    private final ReactiveRedisTemplate<String, ApplicationEntity> redisTemplate;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ApplicationSessionQueryBuilder applicationSessionQueryBuilder;

    @Autowired
    public ApplicationService(ReactiveRedisTemplateFactory redisTemplateFactory, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantJoinRepository participantJoinRepository, ApplicationQueryBuilder applicationQueryBuilder, ApplicationSessionQueryBuilder applicationSessionQueryBuilder) {
        this.redisTemplate = redisTemplateFactory.getTemplate(ApplicationEntity.class);
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantJoinRepository = participantJoinRepository;
        this.applicationQueryBuilder = applicationQueryBuilder;
        this.applicationSessionQueryBuilder = applicationSessionQueryBuilder;
    }

    public Mono<PaginationDto<CollectionDto<ApplicationWithApiKeyDto>>> search(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
        Pageable pageable = applicationSearchPaginationOptionRequest.getPageable();
        Predicate predicate = applicationQueryBuilder.getPredicate(applicationSearchPaginationOptionRequest, hostId);
        OrderSpecifier[] orderSpecifierArray = applicationQueryBuilder.getOrderSpecifier(applicationSearchPaginationOptionRequest);
        Flux<ApplicationEntity> applicationFlux = applicationRepository.query(sqlQuery -> sqlQuery
                .select(QApplicationEntity.applicationEntity)
                .from(QApplicationEntity.applicationEntity)
                .where(predicate)
                .orderBy(orderSpecifierArray)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();
        Flux<ApplicationWithApiKeyDto> applicationDtoFlux = applicationFlux
                .flatMap(application -> applicationSessionRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                        .flatMap(applicationSession -> Mono.just(ApplicationMapper.INSTANCE.applicationAndSessionEntityToWithApiKeyDto(application, applicationSession)))
                        .defaultIfEmpty(ApplicationMapper.INSTANCE.applicationAndSessionEntityToWithApiKeyDto(application, null)));


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
        Function<ApplicationEntity, Mono<ApplicationEntity>> cachingRedis = application -> redisTemplate.opsForValue()
                .set(String.valueOf(application.genRedisKey()), application)
                .thenReturn(application);
        Function<ApplicationEntity, Mono<ApplicationWithApiKeyDto>> result = savedApplication -> Mono.just(ApplicationWithApiKeyDto.builder()
                .applicationId(savedApplication.getId())
                .type(savedApplication.getType())
                .status(savedApplication.getStatus())
                .createdAt(savedApplication.getCreatedAt())
                .apiKey(apiKey)
                .build()
        );

        return applicationRepository.save(ApplicationEntity.builder()
                        .tenantId(hostId)
                        .apiKey(apiKey)
                        .type(applicationCreateRequest.type())
                        .status(Status.PENDING)
                        .build())
                .flatMap(cachingRedis)
                .flatMap(result);
    }

    public Mono<ApplicationDto> startApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> getApplicationEntityMonoWhenValidStatus(application, Status.PENDING, Status.USE))
                .flatMap(application -> Mono.just(ApplicationMapper.INSTANCE.applicationAndSessionEntityToDto(application, null)));
    }

    public Mono<ApplicationDto> endApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> getApplicationEntityMonoWhenValidStatus(application, Status.USE, Status.PENDING))
                .flatMap(application -> Mono.just(ApplicationMapper.INSTANCE.applicationAndSessionEntityToDto(application, null)));
    }

    public Mono<Void> deleteApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> applicationSessionRepository.deleteByApplicationId(applicationId))
                .then(Mono.defer(() -> applicationRepository.deleteById(applicationId)));
    }

    public Mono<PaginationDto<CollectionDto<ApplicationSessionDto>>> searchSessions(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest, UUID applicationId) {
        Pageable pageable = applicationSessionSearchPaginationOptionRequest.getPageable();
        Predicate predicate = applicationSessionQueryBuilder.getPredicate(applicationSessionSearchPaginationOptionRequest, applicationId);
        OrderSpecifier[] orderSpecifierArray = applicationSessionQueryBuilder.getOrderSpecifier(applicationSessionSearchPaginationOptionRequest);

        // TODO: 체인 하나로 묶기
        Flux<ApplicationSessionEntity> applicationSessionFlux = applicationSessionRepository.query(sqlQuery -> sqlQuery
                .select(QApplicationSessionEntity.applicationSessionEntity)
                .from(QApplicationSessionEntity.applicationSessionEntity)
                .where(predicate)
                .orderBy(orderSpecifierArray)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();
        Flux<ApplicationSessionDto> applicationSessionDtoFlux = applicationSessionFlux
                .flatMap(applicationSession -> Mono.just(ApplicationSessionMapper.INSTANCE.entityToDto(applicationSession)));

        // TODO: Redis 캐싱 넣기
        if (applicationSessionSearchPaginationOptionRequest.isFirstView()) {
            return applicationSessionDtoFlux.collectList()
                    .flatMap(applicationSessionDtoList -> applicationSessionRepository.count(predicate)
                            .flatMap(count -> {
                                int totalElementCount = (int) Math.ceil((double) count / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<ApplicationSessionDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(count)
                                        .currentPage(applicationSessionSearchPaginationOptionRequest.getPage())
                                        .data(CollectionDto.<ApplicationSessionDto>builder()
                                                .data(applicationSessionDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return applicationSessionDtoFlux.collectList()
                .flatMap(applicationSessionDtoList -> Mono.just(PaginationDto.<CollectionDto<ApplicationSessionDto>>builder()
                        .currentPage(applicationSessionSearchPaginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<ApplicationSessionDto>builder()
                                .data(applicationSessionDtoList)
                                .build())
                        .build()));
    }

    public Mono<ApplicationSessionDto> startApplicationSession(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> validationForNginxRtmp(application, false))
                .flatMap(this::validationForApplicationSession)
                .flatMap(this::startApplicationSession);
    }

    public Mono<ApplicationSessionDto> endApplicationSession(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(this::validationForApplicationSession)
                .flatMap(this::endApplicationSession);
    }

    public Mono<ApplicationSessionDto> startApplicationSession(NginxRtmpRequest nginxRtmpRequest) {
        return applicationRepository.findByApiKey(nginxRtmpRequest.name())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> validationForNginxRtmp(application, true))
                .flatMap(this::validationForApplicationSession)
                .flatMap(this::startApplicationSession);
    }

    public Mono<ApplicationSessionDto> endApplicationSession(NginxRtmpRequest nginxRtmpRequest) {
        return applicationRepository.findByApiKey(nginxRtmpRequest.name())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> validationForNginxRtmp(application, true))
                .flatMap(this::validationForApplicationSession)
                .flatMap(this::endApplicationSession);
    }

    @NotNull
    private Flux<ApplicationSessionEntity> endRemainApplicationSessions(UUID applicationId) {
        return applicationSessionRepository.findByApplicationIdAndDeletedAtIsNull(applicationId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> {
                    applicationSession.setDeletedAt(LocalDateTime.now());
                    return applicationSessionRepository.save(applicationSession);
                });
    }

    @NotNull
    private Mono<ApplicationEntity> validationForApplicationSession(ApplicationEntity application) {
        if (application.getStatus() != Status.USE) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
        }
        return Mono.just(application);
    }

    @NotNull
    private Mono<ApplicationEntity> getApplicationEntityMonoWhenValidStatus(ApplicationEntity application, Status validationStatus, Status saveStatus) {
        boolean invalidStatus = application.getStatus() != validationStatus;

        if (invalidStatus) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_CAN_NOT_MODIFY));
        }

        application.setStatus(saveStatus);
        return applicationRepository.save(application).thenReturn(application);
    }

    @NotNull
    private Mono<ApplicationSessionDto> startApplicationSession(ApplicationEntity application) {
        ApplicationSessionEntity applicationSessionEntity = ApplicationSessionEntity.builder()
                .applicationId(application.getId())
                .build();
        return endRemainApplicationSessions(application.getId())
                .next()
                .then(applicationSessionRepository.save(applicationSessionEntity))
                .thenMany(applicationSessionRepository.findByApplicationIdAndDeletedAtIsNull(application.getId()))
                .next()
                .flatMap(retrievedApplicationSession -> Mono.just(ApplicationSessionMapper.INSTANCE.entityToDto(retrievedApplicationSession)));
    }

    @NotNull
    private Mono<ApplicationSessionDto> endApplicationSession(ApplicationEntity application) {
        return endRemainApplicationSessions(application.getId())
                .flatMap(applicationSession -> participantJoinRepository
                        .updateAllParticipantJoinsBySessionId(applicationSession.getId())
                        .thenReturn(applicationSession))
                .sort((session1, session2) -> session2.getCreatedAt().compareTo(session1.getCreatedAt()))
                .next()
                .flatMap(applicationSession -> Mono.just(ApplicationSessionMapper.INSTANCE.entityToDto(applicationSession)));
    }

    @NotNull
    private Mono<ApplicationEntity> validationForNginxRtmp(ApplicationEntity application, boolean isNginxRtmp) {
        if (isNginxRtmp && application.getType() != ApplicationType.STREAMING) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_SUPPORTED));
        }
        if (!isNginxRtmp && application.getType() == ApplicationType.STREAMING) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_SUPPORTED));
        }
        return Mono.just(application);
    }
}
