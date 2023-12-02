package com.instream.tenant.domain.participant.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ParticipantJoinRepository extends QuerydslR2dbcRepository<ParticipantJoinEntity, UUID> {
    @Query("UPDATE participant_joins SET updated_at = NOW() WHERE application_session_id = :applicationSessionId")
    Mono<Integer> updateAllParticipantJoinsBySessionId(@Param("id") UUID applicationSessionId);
}
