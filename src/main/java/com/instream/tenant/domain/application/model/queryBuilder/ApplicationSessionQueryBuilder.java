package com.instream.tenant.domain.application.model.queryBuilder;

import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.request.ApplicationBillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPathBase;
import org.springframework.stereotype.Component;
import com.querydsl.core.types.dsl.StringPath;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Component
public class ApplicationSessionQueryBuilder extends DynamicQueryBuilder<ApplicationSessionEntity> {
    public BooleanBuilder getPredicate(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest) {
        BooleanBuilder builder = new BooleanBuilder();

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

    public BooleanBuilder getBillingPredicate(ApplicationBillingSearchPaginationOptionRequest paginationOptionRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QApplicationSessionEntity.applicationSessionEntity.status.ne(Expressions.constant(Status.FORCE_STOPPED.getCode())));
        builder.and(QApplicationSessionEntity.applicationSessionEntity.status.ne(Expressions.constant(Status.DELETED.getCode())));
        builder.and(QApplicationSessionEntity.applicationSessionEntity.applicationId.eq(QApplicationEntity.applicationEntity.id));

        if (paginationOptionRequest.getOption().getStatus() != null) {
            builder.and(QApplicationSessionEntity.applicationSessionEntity.status.eq(Expressions.constant(paginationOptionRequest.getOption().getStatus().getCode())));
        }

        builder.and(getPredicate(ApplicationSessionSearchPaginationOptionRequest.builder()
                .deletedStartAt(paginationOptionRequest.getOption().getStartAt())
                .deletedEndAt(paginationOptionRequest.getOption().getEndAt())
                .build()));

        return builder;
    }

    public OrderSpecifier[] getOrderSpecifier(PaginationOptionRequest paginationOptionRequest) {
        return super.getOrderSpecifier(QApplicationSessionEntity.applicationSessionEntity, paginationOptionRequest);
    }
}
