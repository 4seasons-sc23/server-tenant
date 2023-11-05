package com.instream.tenant.domain.participant.repository;

import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

public class InsertParticipantRepositoryImpl implements InsertParticipantRepository {

    private final R2dbcEntityTemplate template;

    public InsertParticipantRepositoryImpl(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<ParticipantEntity> insert(ParticipantEntity participant) {
        return template.insert(ParticipantEntity.class).using(participant).thenReturn(participant);
    }
}
