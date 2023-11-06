package com.instream.tenant.domain.media.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class MediaService {
    private final ReactiveRedisTemplate<String, ApplicationEntity> redisTemplate;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    @Autowired
    public MediaService(ReactiveRedisTemplateFactory redisTemplateFactory, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantJoinRepository participantJoinRepository) {
        this.redisTemplate = redisTemplateFactory.getTemplate(ApplicationEntity.class);
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantJoinRepository = participantJoinRepository;
    }

    public Mono<ApplicationSessionDto> startLive(String apiKey) {
        return applicationRepository.findByApiKey(apiKey)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean isOff = application.getStatus() == Status.PENDING;

                    if (application.getType() != ApplicationType.STREAMING) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_SUPPORTED));
                    }
                    if (isOff) {
                        return createApplicationSession(application);
                    }

                    return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                });
    }

    public Mono<ApplicationSessionDto> endLive(String apiKey) {
        return applicationRepository.findByApiKey(apiKey)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> {
                    boolean isOn = application.getStatus() == Status.USE;

                    if (application.getType() != ApplicationType.STREAMING) {
                        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_SUPPORTED));
                    }
                    if (isOn) {
                        return deleteApplicationSession(application);
                    }

                    return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                });
    }

    private Mono<ApplicationSessionDto> createApplicationSession(ApplicationEntity application) {
        ApplicationSessionEntity applicationSessionEntity = ApplicationSessionEntity.builder()
                .applicationId(application.getId())
                .build();

        return applicationSessionRepository.save(applicationSessionEntity)
                .flatMap(applicationSession -> {
                    application.setStatus(Status.USE);
                    applicationRepository.save(application).then();
                    return applicationSessionRepository.findById(applicationSession.getId());
                })
                .flatMap(retrievedApplicationSession -> Mono.just(ApplicationSessionDto.builder()
                        .id(retrievedApplicationSession.getId())
                        .createdAt(retrievedApplicationSession.getCreatedAt())
                        .deletedAt(retrievedApplicationSession.getDeletedAt())
                        .build()));
    }

    private Mono<ApplicationSessionDto> deleteApplicationSession(ApplicationEntity application) {
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
                        .thenReturn(ApplicationSessionDto.builder()
                                .id(applicationSession.getId())
                                .createdAt(applicationSession.getCreatedAt())
                                .deletedAt(applicationSession.getDeletedAt())
                                .build()));
    }
}
