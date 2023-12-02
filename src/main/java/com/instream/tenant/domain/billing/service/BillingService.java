package com.instream.tenant.domain.billing.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
import com.instream.tenant.domain.application.infra.mapper.ApplicationSessionMapper;
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationQueryBuilder;
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationSessionQueryBuilder;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.request.CreateBillingRequest;
import com.instream.tenant.domain.billing.domain.request.SummaryBillingRequest;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class BillingService {
    private final double MINIO_BILLING_RATIO = 0.00005;

    private final double CHAT_BILLING_RATIO = 0.001;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ApplicationSessionQueryBuilder applicationSessionQueryBuilder;

    @Autowired
    public BillingService(ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, ApplicationQueryBuilder applicationQueryBuilder, ApplicationSessionQueryBuilder applicationSessionQueryBuilder) {
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.applicationQueryBuilder = applicationQueryBuilder;
        this.applicationSessionQueryBuilder = applicationSessionQueryBuilder;
    }

    public Mono<PaginationDto<CollectionDto<BillingDto>>> searchBilling(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest, UUID hostId) {
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        Pageable pageable = billingSearchPaginationOptionRequest.getPageable();
        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(billingSearchPaginationOptionRequest);
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder()
                .type(billingSearchPaginationOptionRequest.getType())
                .build());
        BooleanBuilder applicationPredicateFromBilling = getApplicationPredicate(hostId, billingSearchPaginationOptionRequest.getApplicationId());
        OrderSpecifier[] orderSpecifierArray = applicationSessionQueryBuilder.getOrderSpecifier(billingSearchPaginationOptionRequest);
        Flux<ApplicationSessionEntity> applicationSessionFlux;
        Flux<ApplicationEntity> applicationMono;
        Flux<BillingDto> billingDtoFlux;

        applicationPredicate.and(applicationPredicateFromBilling);

        applicationSessionFlux = applicationSessionRepository.query(sqlQuery -> sqlQuery
                        .select(qApplicationSession)
                        .from(qApplicationSession)
                        .where(applicationSessionPredicate)
                        .leftJoin(qApplication).on(applicationPredicate)
                        .orderBy(orderSpecifierArray)
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset())
                )
                .all();
        applicationMono = applicationRepository.query(sqlQuery -> sqlQuery
                .select(qApplication)
                .from(qApplicationSession)
                .where(applicationSessionPredicate)
                .leftJoin(qApplication).on(applicationPredicate)
        ).all();
        billingDtoFlux = Flux.zip(applicationMono, applicationSessionFlux)
                .flatMap(objects -> Mono.just(ApplicationSessionMapper.INSTANCE.INSTANCE.entityToBilling(objects.getT2())));

        // TODO: Redis 캐싱 넣기
        if (billingSearchPaginationOptionRequest.isFirstView()) {
            return billingDtoFlux.collectList()
                    .flatMap(billingDtoList -> applicationSessionRepository.query(sqlQuery -> sqlQuery
                                    .select(qApplicationSession.id.count())
                                    .from(qApplicationSession)
                                    .where(applicationSessionPredicate)
                                    .join(qApplication).on(applicationPredicate)
                                    .orderBy(orderSpecifierArray)
                            )
                            .one()
                            .flatMap(totalElementCount -> {
                                long pageCount = (long) Math.ceil((double) totalElementCount / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<BillingDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(pageCount)
                                        .currentPage(billingSearchPaginationOptionRequest.getPage())
                                        .data(CollectionDto.<BillingDto>builder()
                                                .data(billingDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return billingDtoFlux.collectList()
                .flatMap(billingDtoList -> Mono.just(PaginationDto.<CollectionDto<BillingDto>>builder()
                        .currentPage(billingSearchPaginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<BillingDto>builder()
                                .data(billingDtoList)
                                .build())
                        .build()));
    }

    public Mono<ServerResponse> getBillingInfo(UUID hostId, UUID billingId) {
        return Mono.empty();
    }

    public Mono<ServerResponse> getBillingSummary(UUID hostId, SummaryBillingRequest summaryBillingRequest) {
        return Mono.empty();
    }

    public Mono<Void> createBilling(CreateBillingRequest createBillingRequest) {
        return applicationSessionRepository.findById(createBillingRequest.sessionId())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> saveBilling(applicationSession, createBillingRequest.count() * CHAT_BILLING_RATIO))
                .then();
    }

    public Mono<Void> createMinioBilling(UUID sessionId) {
        return applicationSessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> saveBilling(applicationSession, MINIO_BILLING_RATIO))
                .then();
    }

    private BooleanBuilder getApplicationPredicate(UUID hostId, UUID applicationId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (applicationId != null) {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(Expressions.constant(applicationId.toString())));
        } else {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(QApplicationSessionEntity.applicationSessionEntity.applicationId));
        }

        booleanBuilder.and(QApplicationEntity.applicationEntity.tenantId.eq(Expressions.constant(hostId.toString())));

        return booleanBuilder;
    }

    @NotNull
    private Mono<ApplicationSessionEntity> saveBilling(ApplicationSessionEntity applicationSession, double cost) {
        Status billingStatus = applicationSession.isDeleted() ? Status.USE : Status.FORCE_STOPPED;

        applicationSession.setStatus(billingStatus);
        applicationSession.setCost(applicationSession.getCost() + cost);

        return applicationSessionRepository.save(applicationSession);
    }
}
