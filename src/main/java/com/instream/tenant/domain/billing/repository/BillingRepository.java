package com.instream.tenant.domain.billing.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.billing.domain.entity.BillingEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BillingRepository  extends QuerydslR2dbcRepository<BillingEntity, UUID> {
    Mono<BillingEntity> findByApplicationSessionId(UUID applicationSessionId);
}

