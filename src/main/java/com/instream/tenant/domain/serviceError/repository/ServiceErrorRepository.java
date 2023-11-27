package com.instream.tenant.domain.serviceError.repository;

import com.infobip.spring.data.r2dbc.EnableQuerydslR2dbcRepositories;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorEntity;
import reactor.core.publisher.Mono;

@EnableQuerydslR2dbcRepositories
public interface ServiceErrorRepository extends QuerydslR2dbcRepository<ServiceErrorEntity, Long> {
    Mono<ServiceErrorEntity> findByErrorIdAndStatus(Long errorId, Status status);
}
