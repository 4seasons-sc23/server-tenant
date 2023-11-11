package com.instream.tenant.domain.participant.specification;

import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.entity.QParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;

import java.util.UUID;

public class ParticipantJoinSpecification {
    public static Predicate with(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID applicationSessionId) {
        assert (applicationSessionId != null);

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QParticipantJoinEntity.participantJoinEntity.applicationSessionId.eq(Expressions.constant(applicationSessionId.toString())));

        if (participantJoinSearchPaginationOptionRequest.getCreatedStartAt() != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.createdAt.goe(participantJoinSearchPaginationOptionRequest.getCreatedStartAt()));
        }

        if (participantJoinSearchPaginationOptionRequest.getCreatedEndAt() != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.createdAt.loe(participantJoinSearchPaginationOptionRequest.getCreatedEndAt()));
        }

        if (participantJoinSearchPaginationOptionRequest.getDeletedStartAt() != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.updatedAt.goe(participantJoinSearchPaginationOptionRequest.getDeletedStartAt()));
        }

        if (participantJoinSearchPaginationOptionRequest.getDeletedEndAt() != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.updatedAt.loe(participantJoinSearchPaginationOptionRequest.getDeletedEndAt()));
        }

        return builder;
    }
}
