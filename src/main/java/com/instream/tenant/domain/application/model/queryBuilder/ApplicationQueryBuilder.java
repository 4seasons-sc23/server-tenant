package com.instream.tenant.domain.application.model.queryBuilder;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApplicationQueryBuilder extends DynamicQueryBuilder<ApplicationEntity> {
    public BooleanBuilder getPredicate(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QApplicationEntity.applicationEntity.status.ne(Expressions.constant(Status.FORCE_STOPPED.getCode())));
        builder.and(QApplicationEntity.applicationEntity.status.ne(Expressions.constant(Status.DELETED.getCode())));

        if (applicationSearchPaginationOptionRequest.getType() != null) {
            builder.and(QApplicationEntity.applicationEntity.type.eq(Expressions.constant(applicationSearchPaginationOptionRequest.getType().getCode())));
        }

        if (applicationSearchPaginationOptionRequest.getStatus() != null) {
            builder.and(QApplicationEntity.applicationEntity.status.eq(Expressions.constant(applicationSearchPaginationOptionRequest.getStatus().getCode())));
        }

        if (applicationSearchPaginationOptionRequest.getStartAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.goe(applicationSearchPaginationOptionRequest.getStartAt()));
        }

        if (applicationSearchPaginationOptionRequest.getEndAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.loe(applicationSearchPaginationOptionRequest.getEndAt()));
        }

        return builder;
    }

    public OrderSpecifier[] getOrderSpecifier(PaginationOptionRequest paginationOptionRequest) {
        return super.getOrderSpecifier(QApplicationEntity.applicationEntity, paginationOptionRequest);
    }
}


