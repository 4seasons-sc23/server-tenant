package com.instream.tenant.domain.participant.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.participant.domain.entity.ParticipantActionEntity;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;

import java.util.UUID;

public interface ParticipantActionRepository extends QuerydslR2dbcRepository<ParticipantActionEntity, UUID> {
}
