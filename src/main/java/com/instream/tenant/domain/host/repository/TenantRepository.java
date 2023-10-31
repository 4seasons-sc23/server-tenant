package com.instream.tenant.domain.host.repository;

import com.instream.tenant.domain.host.domain.entity.TenantEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TenantRepository extends ReactiveCrudRepository<TenantEntity, UUID> {
}
