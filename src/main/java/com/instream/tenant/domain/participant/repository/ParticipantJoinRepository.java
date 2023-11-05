package com.instream.tenant.domain.participant.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;

import java.util.UUID;

public interface ParticipantJoinRepository extends QuerydslR2dbcRepository<ParticipantJoinEntity, UUID> {
}
