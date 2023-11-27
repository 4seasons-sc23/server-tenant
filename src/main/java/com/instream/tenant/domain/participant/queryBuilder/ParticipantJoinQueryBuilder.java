package com.instream.tenant.domain.participant.queryBuilder;

import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.entity.QParticipantJoinEntity;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ParticipantJoinQueryBuilder extends DynamicQueryBuilder<ParticipantJoinEntity> {
    public Predicate getPredicate(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest, UUID hostId, UUID sessionId) {
        BooleanBuilder builder = new BooleanBuilder();

        if (hostId != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.tenantId.eq(Expressions.constant(hostId.toString())));
        }

        if (sessionId != null) {
            builder.and(QParticipantJoinEntity.participantJoinEntity.applicationSessionId.eq(Expressions.constant(sessionId.toString())));
        }

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

    @NotNull
    public Predicate getPredicateWithTenantAndSessionAndParticipant(UUID tenantId, UUID sessionId, String participantId) {
        BooleanBuilder builder = new BooleanBuilder();
        StringPath tenantIdPath = Expressions.stringPath("tenant_id");
        StringPath sessionIdPath = Expressions.stringPath("application_session_id");

        return builder.and(tenantIdPath.eq(tenantId.toString()))
                .and(sessionIdPath.eq(sessionId.toString()))
                .and(QParticipantJoinEntity.participantJoinEntity.participantId.eq(participantId));
    }

    public OrderSpecifier[] getOrderSpecifier(ParticipantJoinSearchPaginationOptionRequest participantJoinSearchPaginationOptionRequest) {
        return super.getOrderSpecifier(QParticipantJoinEntity.participantJoinEntity, participantJoinSearchPaginationOptionRequest.getSort());
    }
}
