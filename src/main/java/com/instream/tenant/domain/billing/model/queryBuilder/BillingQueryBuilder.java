package com.instream.tenant.domain.billing.model.queryBuilder;

import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.entity.BillingEntity;
import com.instream.tenant.domain.billing.domain.entity.QBillingEntity;
import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.common.model.DynamicQueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.stereotype.Component;

@Component
public class BillingQueryBuilder extends DynamicQueryBuilder<BillingEntity> {
    public BooleanBuilder getBillingPredicate(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(QBillingEntity.billingEntity.status.ne(Expressions.constant(Status.FORCE_STOPPED.getCode())));
        builder.and(QBillingEntity.billingEntity.status.ne(Expressions.constant(Status.DELETED.getCode())));

        if(billingSearchPaginationOptionRequest.getStatus() != null) {
            builder.and(QBillingEntity.billingEntity.status.eq(Expressions.constant(billingSearchPaginationOptionRequest.getStatus().getCode())));
        }

        return builder;
    }

    public OrderSpecifier[] getOrderSpecifier(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest) {
        return super.getOrderSpecifier(QBillingEntity.billingEntity, billingSearchPaginationOptionRequest.getSort());
    }
}


