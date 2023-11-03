package com.instream.tenant.domain.application.model.specification;

import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.util.UUID;

public class ApplicationSpecification {
    public static Predicate with(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        if (applicationSearchPaginationOptionRequest.getType() != null) {
            builder.and(QApplicationEntity.applicationEntity.type.eq(applicationSearchPaginationOptionRequest.getType()));
        }

        if (applicationSearchPaginationOptionRequest.getStatus() != null) {
            builder.and(QApplicationEntity.applicationEntity.status.eq(applicationSearchPaginationOptionRequest.getStatus()));
        }

        if (applicationSearchPaginationOptionRequest.getStartAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.goe(applicationSearchPaginationOptionRequest.getStartAt()));
        }

        if (applicationSearchPaginationOptionRequest.getEndAt() != null) {
            builder.and(QApplicationEntity.applicationEntity.createdAt.loe(applicationSearchPaginationOptionRequest.getEndAt()));
        }

        return builder.getValue();
    }
}
