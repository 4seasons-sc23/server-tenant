package com.instream.tenant.domain.tenant.repository;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import com.instream.tenant.domain.tenant.domain.response.FindAccountResponseDto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TenantRepository extends ReactiveCrudRepository<TenantEntity, UUID> {
    Mono<TenantEntity> findByAccount(String account);

    Mono<TenantEntity> findByAccountAndStatus(String account, Status status);

    Mono<TenantEntity> findByAccountAndPassword(String account, String password);

    Mono<TenantEntity> findByPhoneNumberAndStatus(String phoneNumber, Status status);

    Mono<TenantEntity> findByIdAndStatus(UUID hostId, Status status);
}
