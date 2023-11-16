package com.instream.tenant.domain.participant.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.message.domain.dto.MessageDto;
import com.instream.tenant.domain.participant.domain.entity.QParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.request.SendMessageParticipantRequest;
import com.instream.tenant.domain.participant.specification.ParticipantJoinSpecification;
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
public class MessageService {
    private final TenantRepository tenantRepository;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ParticipantRepository participantRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    @Autowired
    public MessageService(TenantRepository tenantRepository, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ParticipantRepository participantRepository, ParticipantJoinRepository participantJoinRepository) {
        this.tenantRepository = tenantRepository;
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.participantRepository = participantRepository;
        this.participantJoinRepository = participantJoinRepository;
    }

    public Mono<MessageDto> sendMessage(String apiKey, UUID tenantId, String participantId, SendMessageParticipantRequest sendMessageParticipantRequest) {
        return Mono.just(MessageDto.builder().build());
    }
}
