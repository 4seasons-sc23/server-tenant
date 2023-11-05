package com.instream.tenant.domain.participant.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;

import java.util.UUID;

public interface ParticipantRepository extends QuerydslR2dbcRepository<ParticipantEntity, String>, InsertParticipantRepository {
}
