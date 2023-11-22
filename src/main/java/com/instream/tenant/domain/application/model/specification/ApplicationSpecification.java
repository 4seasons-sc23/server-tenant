package com.instream.tenant.domain.application.model.specification;

import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.SortOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationSpecification {
    public static Predicate getPredicate(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
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

    public static List<OrderSpecifier> getOrderSpecifier(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest) {
        return applicationSearchPaginationOptionRequest.getSort().stream()
                .flatMap(sortOptionRequest -> QApplicationEntity.applicationEntity.getColumns().stream()
                        .filter(path -> Objects.equals(sortOptionRequest.name(), path.getMetadata().getName()))
                        .map(path -> createOrderSpecifier(path, sortOptionRequest.option())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    private static OrderSpecifier<?> createOrderSpecifier(Path<?> path, SortOption option) {
        Expression<?> expression = path;
        return switch (option) {
            case ASC ->  new OrderSpecifier(Order.ASC, expression);
            case DESC -> new OrderSpecifier(Order.DESC, expression);
        };
    }
}
