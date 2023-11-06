package com.instream.tenant.domain.participant.repository;

import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import reactor.core.publisher.Mono;

public interface InsertParticipantRepository {
    Mono<ParticipantEntity> insert(ParticipantEntity participant);
}