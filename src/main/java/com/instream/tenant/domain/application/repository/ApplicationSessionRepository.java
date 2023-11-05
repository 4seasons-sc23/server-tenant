package com.instream.tenant.domain.application.repository;


import com.infobip.spring.data.r2dbc.EnableQuerydslR2dbcRepositories;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@EnableQuerydslR2dbcRepositories
public interface ApplicationSessionRepository extends QuerydslR2dbcRepository<ApplicationSessionEntity, UUID> {
    Flux<ApplicationSessionEntity> findBy(Predicate predicate, Pageable pageable);

    Mono<ApplicationSessionEntity> findByIdAndApplicationId(UUID id, UUID applicationId);

    Mono<ApplicationSessionEntity> findTopByApplicationIdOrderByCreatedAtDesc(UUID applicationId);

    Mono<Void> deleteByApplicationId(UUID applicationId);

    Mono<Long> count(Predicate predicate);
}
