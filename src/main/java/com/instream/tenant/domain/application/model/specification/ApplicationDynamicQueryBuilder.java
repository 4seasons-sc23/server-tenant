package com.instream.tenant.domain.application.model.specification;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApplicationDynamicQueryBuilder extends DynamicQueryBuilder<ApplicationEntity> {
    public Predicate getPredicate(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
        BooleanBuilder builder = new BooleanBuilder();

        StringPath tenantIdPath = Expressions.stringPath("tenant_id");
        builder.and(tenantIdPath.eq(hostId.toString()));

        if (applicationSearchPaginationOptionRequest.getType() != null) {
            StringPath typePath = Expressions.stringPath("type");
            builder.and(typePath.eq(applicationSearchPaginationOptionRequest.getType().getCode()));
        }

        if (applicationSearchPaginationOptionRequest.getStatus() != null) {
            StringPath statusPath = Expressions.stringPath("status");
            builder.and(statusPath.eq(applicationSearchPaginationOptionRequest.getStatus().getCode()));
        }

        if (applicationSearchPaginationOptionRequest.getStartAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.goe(applicationSearchPaginationOptionRequest.getStartAt()));
        }

        if (applicationSearchPaginationOptionRequest.getEndAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.loe(applicationSearchPaginationOptionRequest.getEndAt()));
        }

        return builder.getValue();
    }

    public List<OrderSpecifier> getOrderSpecifier(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest) {
        return super.getOrderSpecifier(QApplicationEntity.applicationEntity, applicationSearchPaginationOptionRequest.getSort());
    }
}
