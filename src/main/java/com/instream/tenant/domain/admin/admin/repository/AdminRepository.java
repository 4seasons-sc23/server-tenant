package com.instream.tenant.domain.admin.admin.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.admin.admin.domain.entity.AdminEntity;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import reactor.core.publisher.Mono;

public interface AdminRepository extends QuerydslR2dbcRepository<AdminEntity, Long> {
    Mono<AdminEntity> findByAccountAndPassword(String account, String password);
}
