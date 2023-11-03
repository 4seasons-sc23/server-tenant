package com.instream.tenant.domain.application.repository;


import com.infobip.spring.data.r2dbc.EnableQuerydslR2dbcRepositories;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@EnableQuerydslR2dbcRepositories
public interface ApplicationRepository extends QuerydslR2dbcRepository<ApplicationEntity, UUID> {
    Flux<ApplicationEntity> findBy(Predicate predicate, Pageable pageable);

    Mono<Long> count(Predicate predicate);
}
