package com.instream.tenant.domain.application.model.specification;

import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ApplicationSessionSpecification {
    public static Predicate with(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest, UUID applicationId) {
        assert (applicationId != null);

        BooleanBuilder builder = new BooleanBuilder();

        StringPath stringPath = Expressions.stringPath("application_id");
        builder.and(stringPath.eq(String.valueOf(applicationId)));

        if (applicationSessionSearchPaginationOptionRequest.getCreatedStartAt() != null) {
            builder.and(QApplicationSessionEntity.applicationSessionEntity.createdAt.goe(applicationSessionSearchPaginationOptionRequest.getCreatedStartAt()));
        }

        if (applicationSessionSearchPaginationOptionRequest.getCreatedEndAt() != null) {
            builder.and(QApplicationSessionEntity.applicationSessionEntity.createdAt.loe(applicationSessionSearchPaginationOptionRequest.getCreatedEndAt()));
        }

        if (applicationSessionSearchPaginationOptionRequest.getDeletedStartAt() != null) {
            builder.and(QApplicationSessionEntity.applicationSessionEntity.deletedAt.goe(applicationSessionSearchPaginationOptionRequest.getDeletedStartAt()));
        }

        if (applicationSessionSearchPaginationOptionRequest.getDeletedEndAt() != null) {
            builder.and(QApplicationSessionEntity.applicationSessionEntity.deletedAt.loe(applicationSessionSearchPaginationOptionRequest.getDeletedEndAt()));
        }

        return builder;
    }
}
