package com.instream.tenant.domain.application.model.specification;

import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ApplicationSessionQueryBuilder extends DynamicQueryBuilder<ApplicationSessionEntity> {
    public Predicate getPredicate(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest, UUID applicationId) {
        assert (applicationId != null);

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QApplicationSessionEntity.applicationSessionEntity.applicationId.eq(Expressions.constant(applicationId.toString())));

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

    public OrderSpecifier[] getOrderSpecifier(ApplicationSessionSearchPaginationOptionRequest applicationSessionSearchPaginationOptionRequest) {
        System.out.println(applicationSessionSearchPaginationOptionRequest);
        return super.getOrderSpecifier(QApplicationSessionEntity.applicationSessionEntity, applicationSessionSearchPaginationOptionRequest.getSort());
    }
}
