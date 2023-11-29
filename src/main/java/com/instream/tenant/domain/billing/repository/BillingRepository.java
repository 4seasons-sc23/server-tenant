package com.instream.tenant.domain.billing.repository;

import com.instream.tenant.domain.billing.domain.entity.BillingEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface BillingRepository  extends ReactiveCrudRepository<BillingEntity, UUID> {
}
