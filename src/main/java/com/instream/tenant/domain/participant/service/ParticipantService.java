package com.instream.tenant.domain.participant.service;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.domain.dto.MessageDto;
import com.instream.tenant.domain.participant.domain.entity.QParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.request.SendMessageParticipantRequest;
import com.instream.tenant.domain.participant.infra.mapper.ParticipantJoinMapper;
import com.instream.tenant.domain.participant.queryBuilder.ParticipantJoinQueryBuilder;
import com.instream.tenant.domain.participant.repository.ParticipantActionRepository;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.infra.enums.ParticipantErrorCode;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.participant.repository.ParticipantRepository;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantService {
    private final ParticipantJoinQueryBuilder participantJoinQueryBuilder;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantRepository participantRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    private final ParticipantActionRepository participantActionRepository;

    @Autowired
    public ParticipantService(ParticipantJoinQueryBuilder participantJoinQueryBuilder, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantRepository participantRepository, ParticipantJoinRepository participantJoinRepository, ParticipantActionRepository participantActionRepository) {
        this.participantJoinQueryBuilder = participantJoinQueryBuilder;
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantRepository = participantRepository;
        this.participantJoinRepository = participantJoinRepository;
        this.participantActionRepository = participantActionRepository;
    }

    public Mono<ParticipantJoinDto> enterToApplication(String apiKey, UUID sessionId, EnterToApplicationParticipantRequest enterToApplicationParticipantRequest) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정


        return getApplicationEntityMonoFromApiKeyAndSessionId(apiKey, sessionId)
                .flatMap(application -> saveOrUpdateParticipant(enterToApplicationParticipantRequest, application)
                        .flatMap(participant -> participantRepository.findById(participant.getId()))
                        .flatMap(participant -> deleteRemainParticipantSession(sessionId, participant)
                                .then(participantJoinRepository.save(ParticipantJoinEntity.builder()
                                        .tenantId(application.getTenantId())
                                        .participantId(participant.getId())
                                        .applicationSessionId(sessionId)
                                        .build()))
                                .flatMap(participantJoin -> participantJoinRepository.findById(participantJoin.getId()))
                                .flatMap(participantJoin -> Mono.just(ParticipantJoinMapper.INSTANCE.participantAndParticipantJoinAndApplicationToDto(participant, participantJoin, null)))));
    }

    public Mono<ParticipantJoinDto> leaveFromApplication(String apiKey, UUID sessionId, LeaveFromApplicationParticipantRequest leaveFromApplicationParticipantRequest) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정

        return getApplicationEntityMonoFromApiKeyAndSessionId(apiKey, sessionId)
                .then(participantRepository.findById(leaveFromApplicationParticipantRequest.participantId())
                        .switchIfEmpty(Mono.error(new RestApiException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND)))
                        .flatMap(participant -> getParticipantJoinWithTenantAndSessionAndParticipant(participant.getTenantId(), sessionId, participant.getId())
                                .flatMap(participantJoin -> {
                                    if (participantJoin.isDeleted()) {
                                        return Mono.just(participantJoin);
                                    }
                                    participantJoin.setUpdatedAt(LocalDateTime.now());
                                    return participantJoinRepository.save(participantJoin);
                                })
                                .next()
                                .flatMap(participantJoin -> Mono.just(ParticipantJoinMapper.INSTANCE.participantAndParticipantJoinAndApplicationToDto(participant, participantJoin, null))))
                );
    }

    public Mono<PaginationDto<CollectionDto<ParticipantJoinDto>>> searchParticipantJoin(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId, UUID sessionId) {
        Pageable pageable = participantJoinSearchPaginationOptionRequest.getPageable();
        Predicate predicate = participantJoinQueryBuilder.getPredicate(participantJoinSearchPaginationOptionRequest, hostId, sessionId);
        Flux<ParticipantJoinEntity> participantJoinFlux = participantJoinRepository.query(sqlQuery -> sqlQuery
                .select(QParticipantJoinEntity.participantJoinEntity)
                .from(QParticipantJoinEntity.participantJoinEntity)
                .where(predicate)
                .orderBy(QParticipantJoinEntity.participantJoinEntity.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();
        Flux<ParticipantJoinDto> participantJoinDtoFlux = participantJoinFlux
                .flatMap(participantJoin -> applicationSessionRepository.findById(participantJoin.getApplicationSessionId())
                        .flatMap(applicationSession ->
                                Mono.zip(
                                        participantRepository.findById(participantJoin.getParticipantId()),
                                        applicationRepository.findById(applicationSession.getApplicationId())
                                ).map(tuple -> {
                                    ParticipantEntity participant = tuple.getT1();
                                    ApplicationEntity application = tuple.getT2();
                                    return ParticipantJoinMapper.INSTANCE.participantAndParticipantJoinAndApplicationToDto(participant, participantJoin, ApplicationMapper.INSTANCE.applicationAndSessionEntityToDto(application, applicationSession));
                                })
                        ));

        // TODO: Redis 캐싱 넣기
        if (participantJoinSearchPaginationOptionRequest.isFirstView()) {
            return participantJoinDtoFlux.collectList()
                    .flatMap(participantJoinDtoList -> participantJoinRepository.count(predicate)
                            .flatMap(totalElementCount -> {
                                long pageCount = (long) Math.ceil((double) totalElementCount / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<ParticipantJoinDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(pageCount)
                                        .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                                        .data(CollectionDto.<ParticipantJoinDto>builder()
                                                .data(participantJoinDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return participantJoinDtoFlux.collectList()
                .flatMap(participantJoinDtoList -> Mono.just(PaginationInfoDto.<CollectionDto<ParticipantJoinDto>>builder()
                        .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<ParticipantJoinDto>builder()
                                .data(participantJoinDtoList)
                                .build())
                        .build()));
    }

    public Mono<MessageDto> sendMessage(String apiKey, UUID tenantId, String participantId, SendMessageParticipantRequest sendMessageParticipantRequest) {
        return Mono.just(MessageDto.builder().build());
    }

    @NotNull
    private Mono<ApplicationEntity> getApplicationEntityMonoFromApiKeyAndSessionId(String apiKey, UUID sessionId) {
        return applicationSessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> {
                    if (applicationSession.isDeleted()) {
                        return Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_ALREADY_ENDED));
                    }
                    return applicationRepository.findById(applicationSession.getApplicationId());
                })
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                .flatMap(application -> application.validateApiKey(apiKey))
                .flatMap(ApplicationEntity::toMonoWhenIsOn);
    }

    @NotNull
    private Mono<ParticipantEntity> saveOrUpdateParticipant(EnterToApplicationParticipantRequest enterToApplicationParticipantRequest, ApplicationEntity application) {
        return participantRepository.findById(enterToApplicationParticipantRequest.participantId())
                .flatMap(savedParticipant -> {
                    savedParticipant.setNickname(enterToApplicationParticipantRequest.nickname());
                    savedParticipant.setProfileImgUrl(enterToApplicationParticipantRequest.profileImgUrl());
                    return participantRepository.save(savedParticipant);
                })
                .switchIfEmpty(participantRepository.insert(ParticipantEntity.builder()
                        .id(enterToApplicationParticipantRequest.participantId())
                        .tenantId(application.getTenantId())
                        .nickname(enterToApplicationParticipantRequest.nickname())
                        .profileImgUrl(enterToApplicationParticipantRequest.profileImgUrl())
                        .build()));
    }

    @NotNull
    private Flux<ParticipantJoinEntity> deleteRemainParticipantSession(UUID sessionId, ParticipantEntity participant) {
        return getParticipantJoinWithTenantAndSessionAndParticipant(participant.getTenantId(), sessionId, participant.getId())
                .flatMap(participantJoin -> {
                    participantJoin.setUpdatedAt(LocalDateTime.now());
                    return participantJoinRepository.save(participantJoin);
                });
    }

    @NotNull
    private Flux<ParticipantJoinEntity> getParticipantJoinWithTenantAndSessionAndParticipant(UUID tenantId, UUID sessionId, String participantId) {
        return participantJoinRepository.query(sqlQuery -> sqlQuery
                .select(QParticipantJoinEntity.participantJoinEntity)
                .from(QParticipantJoinEntity.participantJoinEntity)
                .where(participantJoinQueryBuilder.getPredicateWithTenantAndSessionAndParticipant(tenantId, sessionId, participantId))
                .orderBy(new OrderSpecifier<>(Order.DESC, QParticipantJoinEntity.participantJoinEntity.createdAt))
        ).all();
    }
}
