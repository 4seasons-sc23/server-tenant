package com.instream.tenant.domain.common.model;

import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.SortOption;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPathBase;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class DynamicQueryBuilder<T> {
    protected OrderSpecifier[] getOrderSpecifier(RelationalPathBase<T> relationalPathBase, Collection<SortOptionRequest> sortOptionRequests) {
        if(sortOptionRequests == null) {
            return null;
        }
        return sortOptionRequests.stream()
                .flatMap(sortOptionRequest -> relationalPathBase.getColumns().stream()
                        .filter(path -> Objects.equals(sortOptionRequest.name(), path.getMetadata().getName()))
                        .map(path -> createOrderSpecifier(path, sortOptionRequest.option())))
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> createOrderSpecifier(Path<?> path, SortOption option) {
        Expression<?> expression = path;
        return switch (option) {
            case ASC -> new OrderSpecifier(Order.ASC, expression);
            case DESC -> new OrderSpecifier(Order.DESC, expression);
        };
    }
}
