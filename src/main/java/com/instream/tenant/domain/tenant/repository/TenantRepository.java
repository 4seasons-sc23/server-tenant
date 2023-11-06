package com.instream.tenant.domain.tenant.repository;

import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TenantRepository extends ReactiveCrudRepository<TenantEntity, UUID> {
    Mono<TenantEntity> findByAccount(String account);

    Mono<TenantEntity> findByAccountAndPassword(String account, String password);
}
