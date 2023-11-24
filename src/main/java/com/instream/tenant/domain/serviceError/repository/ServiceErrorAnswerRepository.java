package com.instream.tenant.domain.serviceError.repository;

import com.infobip.spring.data.r2dbc.EnableQuerydslR2dbcRepositories;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorAnswerEntity;
import reactor.core.publisher.Mono;

@EnableQuerydslR2dbcRepositories
public interface ServiceErrorAnswerRepository extends
    QuerydslR2dbcRepository<ServiceErrorAnswerEntity, Long> {
    Mono<ServiceErrorAnswerEntity> findByErrorId(Long errorId);
}