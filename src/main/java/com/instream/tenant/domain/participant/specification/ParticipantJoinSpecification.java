package com.instream.tenant.domain.participant.specification;

import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.entity.QParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import java.util.UUID;

public class ParticipantJoinSpecification {
    public static Predicate with(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId) {
        BooleanBuilder builder = new BooleanBuilder();

        StringPath stringPath = Expressions.stringPath("tenant_id");
        builder.and(stringPath.eq(String.valueOf(hostId)));

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

    public static Predicate with(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId, UUID applicationSessionId) {
        assert (applicationSessionId != null);

        BooleanBuilder builder = new BooleanBuilder();

        StringPath tenantPath = Expressions.stringPath("tenant_id");
        builder.and(tenantPath.eq(String.valueOf(hostId)));

        StringPath applicationSessionPath = Expressions.stringPath("application_session_id");
        builder.and(applicationSessionPath.eq(String.valueOf(applicationSessionId)));


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
