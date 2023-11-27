package com.instream.tenant.domain.participant.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
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
import com.instream.tenant.domain.participant.repository.ParticipantActionRepository;
import com.instream.tenant.domain.participant.specification.ParticipantJoinSpecification;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import com.instream.tenant.domain.tenant.repository.TenantRepository;
import com.instream.tenant.domain.participant.domain.dto.ParticipantDto;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.infra.enums.ParticipantErrorCode;
import com.instream.tenant.domain.participant.infra.enums.ParticipantJoinErrorCode;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.participant.repository.ParticipantRepository;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantService {
    private final TenantRepository tenantRepository;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantRepository participantRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    private final ParticipantActionRepository participantActionRepository;

    @Autowired
    public ParticipantService(TenantRepository tenantRepository, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantRepository participantRepository, ParticipantJoinRepository participantJoinRepository, ParticipantActionRepository participantActionRepository) {
        this.tenantRepository = tenantRepository;
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantRepository = participantRepository;
        this.participantJoinRepository = participantJoinRepository;
        this.participantActionRepository = participantActionRepository;
    }

    public Mono<ParticipantJoinDto> enterToApplication(UUID tenantId, String participantId, EnterToApplicationParticipantRequest enterToApplicationParticipantRequest) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정
        // TODO: 현재 Reactive chain이 너무 길어지는 문제 발생. 검증 로직은 Validator로 구현하고, ReactiveValidator는 Validator랑 composite하는 형태로 구현하기

        Mono<TenantEntity> tenantMono = tenantRepository.findById(tenantId)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)));
        Mono<ApplicationEntity> applicationMono = applicationSessionRepository.findById(enterToApplicationParticipantRequest.applicationSessionId())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> applicationRepository.findById(applicationSession.getApplicationId())
                        .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)))
                );
        Mono<ParticipantEntity> createOrUpdateParticipantMono = participantRepository.findById(participantId)
                .flatMap(savedParticipant -> {
                    savedParticipant.setNickname(enterToApplicationParticipantRequest.nickname());
                    savedParticipant.setProfileImgUrl(enterToApplicationParticipantRequest.profileImgUrl());
                    return participantRepository.save(savedParticipant);
                })
                .switchIfEmpty(participantRepository.insert(ParticipantEntity.builder()
                        .id(participantId)
                        .tenantId(tenantId)
                        .nickname(enterToApplicationParticipantRequest.nickname())
                        .profileImgUrl(enterToApplicationParticipantRequest.profileImgUrl())
                        .build()));

        return tenantMono.then(
                applicationMono.flatMap(application -> createOrUpdateParticipantMono
                        .flatMap(participant -> participantRepository.findById(participantId))
                        .flatMap(participant -> participantJoinRepository.findByTenantIdAndParticipantIdAndApplicationSessionIdAndUpdatedAtIsNullOrderByCreatedAtDesc(tenantId, participantId, enterToApplicationParticipantRequest.applicationSessionId())
                                .flatMap(participantJoin -> {
                                    participantJoin.setUpdatedAt(LocalDateTime.now());
                                    return participantJoinRepository.save(participantJoin);
                                })
                                .then(participantJoinRepository.save(ParticipantJoinEntity.builder()
                                                .tenantId(tenantId)
                                                .participantId(participantId)
                                                .applicationSessionId(enterToApplicationParticipantRequest.applicationSessionId())
                                                .build())
                                        .flatMap(participantJoin -> participantJoinRepository.findById(participantJoin.getId())))
                                .flatMap(participantJoin -> Mono.just(ParticipantJoinDto.builder()
                                        .id(participantJoin.getId())
                                        .createdAt(participantJoin.getCreatedAt())
                                        .updatedAt(participantJoin.getUpdatedAt())
                                        .participant(ParticipantDto.builder()
                                                .id(participant.getId())
                                                .tenantId(participant.getTenantId())
                                                .nickname(participant.getNickname())
                                                .profileImgUrl(participant.getProfileImgUrl())
                                                .createdAt(participant.getCreatedAt())
                                                .build())
                                        .build())))
                ));
    }

    public Mono<ParticipantJoinDto> leaveFromApplication(String apiKey, UUID tenantId, String
            participantId, LeaveFromApplicationParticipantRequest leaveFromApplicationParticipantRequest) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정
        // TODO: 현재 Reactive chain이 너무 길어지는 문제 발생. 검증 로직은 Validator로 구현하고, ReactiveValidator는 Validator랑 composite하는 형태로 구현하기
        // TODO: method extract 해서 enterToApplication랑 통합

        return tenantRepository.existsById(tenantId).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND));
            }

            return applicationSessionRepository.findById(leaveFromApplicationParticipantRequest.applicationSessionId())
                    .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                    .flatMap(applicationSession -> {

                        // 세션이 종료될 때는 세션 종료 API에서 leave timestampe를 수정합니다.
                        if (applicationSession.getDeletedAt() != null) {
                            return Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_ALREADY_ENDED));
                        }

                        Mono<ParticipantEntity> participantEntityMono = participantRepository.findById(participantId)
                                .switchIfEmpty(Mono.error(new RestApiException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND)));
                        Mono<ParticipantJoinEntity> participantJoinEntityMono = participantJoinRepository.findByTenantIdAndParticipantIdAndApplicationSessionIdAndUpdatedAtIsNullOrderByCreatedAtDesc(tenantId, participantId, leaveFromApplicationParticipantRequest.applicationSessionId())
                                .switchIfEmpty(Mono.error(new RestApiException(ParticipantJoinErrorCode.PARTICIPANT_JOIN_NOT_FOUND)))
                                .flatMap(participantJoin -> {
                                    participantJoin.setUpdatedAt(LocalDateTime.now());
                                    return participantJoinRepository.save(participantJoin);
                                })
                                .next()
                                .flatMap(participantJoin -> participantJoinRepository.findById(participantJoin.getId()));

                        return Mono.zip(participantEntityMono, participantJoinEntityMono, (participant, participantJoin) -> ParticipantJoinDto.builder()
                                .id(participantJoin.getId())
                                .createdAt(participantJoin.getCreatedAt())
                                .updatedAt(participantJoin.getUpdatedAt())
                                .participant(ParticipantDto.builder()
                                        .id(participant.getId())
                                        .tenantId(participant.getTenantId())
                                        .nickname(participant.getNickname())
                                        .profileImgUrl(participant.getProfileImgUrl())
                                        .createdAt(participant.getCreatedAt())
                                        .build())
                                .build());
                    });
        });
    }

    public Mono<PaginationDto<CollectionDto<ParticipantJoinDto>>> searchParticipantJoin(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId) {
        // TODO: 호스트 인증 넣기

        Pageable pageable = participantJoinSearchPaginationOptionRequest.getPageable();
        Predicate predicate = ParticipantJoinSpecification.with(participantJoinSearchPaginationOptionRequest, hostId);

        // TODO: 체인 하나로 묶기
        Flux<ParticipantJoinEntity> participantJoinFlux = participantJoinRepository.query(sqlQuery -> sqlQuery
                .select(QParticipantJoinEntity.participantJoinEntity)
                .from(QParticipantJoinEntity.participantJoinEntity)
                .where(predicate)
                .orderBy(QParticipantJoinEntity.participantJoinEntity.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();

        Mono<List<ParticipantJoinDto>> applicationSessionDtoListMono = participantJoinFlux
                .flatMap(participantJoin -> applicationSessionRepository.findById(participantJoin.getApplicationSessionId())
                        .flatMap(applicationSession ->
                                Mono.zip(
                                        participantRepository.findById(participantJoin.getParticipantId()),
                                        applicationRepository.findById(applicationSession.getApplicationId())
                                ).map(tuple -> {
                                    ParticipantEntity participant = tuple.getT1();
                                    ApplicationEntity application = tuple.getT2();

                                    return ParticipantJoinDto.builder()
                                            .id(participantJoin.getId())
                                            .createdAt(participantJoin.getCreatedAt())
                                            .updatedAt(participantJoin.getUpdatedAt())
                                            .participant(ParticipantDto.builder()
                                                    .id(participant.getId())
                                                    .nickname(participant.getNickname())
                                                    .profileImgUrl(participant.getProfileImgUrl())
                                                    .createdAt(participant.getCreatedAt())
                                                    .build())
                                            .application(ApplicationDto.builder()
                                                    .applicationId(application.getId())
                                                    .type(application.getType())
                                                    .status(application.getStatus())
                                                    .createdAt(application.getCreatedAt())
                                                    .session(ApplicationSessionDto.builder()
                                                            .id(applicationSession.getId())
                                                            .createdAt(applicationSession.getCreatedAt())
                                                            .deletedAt(applicationSession.getDeletedAt())
                                                            .build())
                                                    .build())
                                            .build();
                                })
                        )
                )
                .collectList();


        // TODO: Redis 캐싱 넣기
        if (participantJoinSearchPaginationOptionRequest.isFirstView()) {
            Mono<Long> totalElementCountMono = participantJoinRepository.count(predicate);
            Mono<Integer> totalPageCountMono = totalElementCountMono.map(count -> (int) Math.ceil((double) count / pageable.getPageSize()));

            return Mono.zip(applicationSessionDtoListMono, totalPageCountMono, totalElementCountMono)
                    .map(tuple -> {
                        List<ParticipantJoinDto> participantJoinDtoList = tuple.getT1();
                        Integer pageCount = tuple.getT2();
                        int totalElementCount = tuple.getT3().intValue();

                        return PaginationInfoDto.<CollectionDto<ParticipantJoinDto>>builder()
                                .totalElementCount(totalElementCount)
                                .pageCount(pageCount)
                                .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                                .data(CollectionDto.<ParticipantJoinDto>builder()
                                        .data(participantJoinDtoList)
                                        .build())
                                .build();
                    });
        }

        return applicationSessionDtoListMono.map(participantJoinDtoList -> PaginationDto.<CollectionDto<ParticipantJoinDto>>builder()
                .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                .data(CollectionDto.<ParticipantJoinDto>builder()
                        .data(participantJoinDtoList)
                        .build())
                .build()
        );
    }

    public Mono<PaginationDto<CollectionDto<ParticipantJoinDto>>> searchParticipantJoinWithApplication(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId, UUID applicationSessionId) {
        // TODO: 호스트 인증 넣기

        Pageable pageable = participantJoinSearchPaginationOptionRequest.getPageable();
        Predicate predicate = ParticipantJoinSpecification.with(participantJoinSearchPaginationOptionRequest, hostId, applicationSessionId);

        // TODO: 체인 하나로 묶기
        Flux<ParticipantJoinEntity> participantJoinFlux = participantJoinRepository.query(sqlQuery -> sqlQuery
                .select(QParticipantJoinEntity.participantJoinEntity)
                .from(QParticipantJoinEntity.participantJoinEntity)
                .where(predicate)
                .orderBy(QParticipantJoinEntity.participantJoinEntity.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();

        Mono<List<ParticipantJoinDto>> applicationSessionDtoListMono = participantJoinFlux
                .flatMap(participantJoin -> applicationSessionRepository.findById(applicationSessionId)
                        .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                        .flatMap(applicationSession -> {
                            Mono<ParticipantEntity> participantMono = participantRepository.findById(participantJoin.getParticipantId())
                                    .switchIfEmpty(Mono.error(new RestApiException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND)));
                            Mono<ApplicationEntity> applicationMono = applicationRepository.findById(applicationSession.getApplicationId())
                                    .switchIfEmpty(Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_FOUND)));

                            return Mono.zip(participantMono, applicationMono, (participant, application) ->
                                    ParticipantJoinDto.builder()
                                            .id(participantJoin.getId())
                                            .createdAt(participantJoin.getCreatedAt())
                                            .updatedAt(participantJoin.getUpdatedAt())
                                            .participant(ParticipantDto.builder()
                                                    .id(participant.getId())
                                                    .nickname(participant.getNickname())
                                                    .profileImgUrl(participant.getProfileImgUrl())
                                                    .createdAt(participant.getCreatedAt())
                                                    .build())
                                            .application(ApplicationDto.builder()
                                                    .applicationId(application.getId())
                                                    .type(application.getType())
                                                    .status(application.getStatus())
                                                    .createdAt(application.getCreatedAt())
                                                    .session(ApplicationSessionDto.builder()
                                                            .id(applicationSession.getId())
                                                            .createdAt(applicationSession.getCreatedAt())
                                                            .deletedAt(applicationSession.getDeletedAt())
                                                            .build())
                                                    .build())
                                            .build()
                            );
                        }))
                .collectList();

        // TODO: Redis 캐싱 넣기
        if (participantJoinSearchPaginationOptionRequest.isFirstView()) {
            Mono<Long> totalElementCountMono = participantJoinRepository.count(predicate);
            Mono<Integer> totalPageCountMono = totalElementCountMono.map(count -> (int) Math.ceil((double) count / pageable.getPageSize()));

            return Mono.zip(applicationSessionDtoListMono, totalPageCountMono, totalElementCountMono)
                    .map(tuple -> {
                        List<ParticipantJoinDto> participantJoinDtoList = tuple.getT1();
                        Integer pageCount = tuple.getT2();
                        int totalElementCount = tuple.getT3().intValue();

                        return PaginationInfoDto.<CollectionDto<ParticipantJoinDto>>builder()
                                .totalElementCount(totalElementCount)
                                .pageCount(pageCount)
                                .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                                .data(CollectionDto.<ParticipantJoinDto>builder()
                                        .data(participantJoinDtoList)
                                        .build())
                                .build();
                    });
        }

        return applicationSessionDtoListMono.map(participantJoinDtoList -> PaginationDto.<CollectionDto<ParticipantJoinDto>>builder()
                .currentPage(participantJoinSearchPaginationOptionRequest.getPageable().getPageNumber())
                .data(CollectionDto.<ParticipantJoinDto>builder()
                        .data(participantJoinDtoList)
                        .build())
                .build()
        );
    }

    public Mono<MessageDto> sendMessage(String apiKey, UUID tenantId, String participantId, SendMessageParticipantRequest sendMessageParticipantRequest) {
        return Mono.just(MessageDto.builder().build());
    }
}
