package com.instream.tenant.domain.participant.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ParticipantJoinRepository extends QuerydslR2dbcRepository<ParticipantJoinEntity, UUID> {
    Mono<ParticipantJoinEntity> findByTenantIdAndParticipantIdAndSessionId(UUID tenantId, String participantId, UUID sessionId);
}
