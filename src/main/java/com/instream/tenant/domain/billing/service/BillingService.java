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
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationQueryBuilder;
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationSessionQueryBuilder;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.entity.BillingEntity;
import com.instream.tenant.domain.billing.domain.entity.QBillingEntity;
import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.request.CreateBillingRequest;
import com.instream.tenant.domain.billing.infra.mapper.BillingMapper;
import com.instream.tenant.domain.billing.model.queryBuilder.BillingQueryBuilder;
import com.instream.tenant.domain.billing.repository.BillingRepository;
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
    private final BillingRepository billingRepository;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final BillingQueryBuilder billingQueryBuilder;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ApplicationSessionQueryBuilder applicationSessionQueryBuilder;

    @Autowired
    public BillingService(BillingRepository billingRepository, ApplicationRepository applicationRepository, ApplicationSessionRepository applicationSessionRepository, BillingQueryBuilder billingQueryBuilder, ApplicationQueryBuilder applicationQueryBuilder, ApplicationSessionQueryBuilder applicationSessionQueryBuilder) {
        this.billingRepository = billingRepository;
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.billingQueryBuilder = billingQueryBuilder;
        this.applicationQueryBuilder = applicationQueryBuilder;
        this.applicationSessionQueryBuilder = applicationSessionQueryBuilder;
    }

    public Mono<PaginationDto<CollectionDto<BillingDto>>> searchBilling(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest, UUID hostId) {
        QBillingEntity qBilling = QBillingEntity.billingEntity;
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        Pageable pageable = billingSearchPaginationOptionRequest.getPageable();
        BooleanBuilder billingPredicate = billingQueryBuilder.getBillingPredicate(billingSearchPaginationOptionRequest);
        BooleanBuilder applicationSessionPredicate = getApplicationSessionPredicate(billingSearchPaginationOptionRequest);
        BooleanBuilder applicationPredicate = getApplicationPredicate(billingSearchPaginationOptionRequest, hostId);
        OrderSpecifier[] orderSpecifierArray = billingQueryBuilder.getOrderSpecifier(billingSearchPaginationOptionRequest);

        Flux<BillingEntity> billingFlux = billingRepository.query(sqlQuery -> sqlQuery
                        .select(qBilling)
                        .from(qBilling)
                        .where(billingPredicate)
                        .leftJoin(qApplicationSession).on(applicationSessionPredicate)
                        .leftJoin(qApplication).on(applicationPredicate)
                        .orderBy(orderSpecifierArray)
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset())
                )
                .all();
        Mono<ApplicationSessionEntity> applicationSessionFlux = applicationSessionRepository.query(sqlQuery -> sqlQuery
                .select(qApplicationSession)
                .from(qBilling)
                .where(billingPredicate)
                .leftJoin(qApplicationSession).on(applicationSessionPredicate)
        ).one();
        Mono<ApplicationEntity> applicationFlux = applicationRepository.query(sqlQuery -> sqlQuery
                .select(qApplication)
                .from(qBilling)
                .where(billingPredicate)
                .leftJoin(qApplicationSession).on(applicationSessionPredicate)
                .leftJoin(qApplication).on(applicationPredicate)
        ).one();
        Flux<BillingDto> billingDtoFlux = Flux.zip(billingFlux, applicationFlux, applicationSessionFlux)
                .flatMap(objects -> {
                    ApplicationDto applicationDto = ApplicationMapper.INSTANCE.applicationAndSessionEntityToDto(objects.getT2(), objects.getT3());
                    BillingDto billingDto = BillingMapper.INSTANCE.billingAndApplicationToDto(objects.getT1(), applicationDto);
                    return Mono.just(billingDto);
                });

        // TODO: Redis 캐싱 넣기
        if (billingSearchPaginationOptionRequest.isFirstView()) {
            return billingDtoFlux.collectList()
                    .flatMap(billingDtoList -> billingRepository.query(sqlQuery -> sqlQuery
                                    .select(qBilling.id.count())
                                    .from(qBilling)
                                    .where(billingPredicate)
                                    .join(qApplicationSession).on(applicationSessionPredicate)
                                    .join(qApplication).on(applicationPredicate)
                                    .orderBy(orderSpecifierArray)
                                    .limit(pageable.getPageSize())
                                    .offset(pageable.getOffset())
                            ).one()
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

    private BooleanBuilder getApplicationSessionPredicate(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest) {
        BooleanBuilder booleanBuilder = applicationSessionQueryBuilder.getPredicate(ApplicationSessionSearchPaginationOptionRequest.builder()
                .createdStartAt(billingSearchPaginationOptionRequest.getCreatedStartAt())
                .createdEndAt(billingSearchPaginationOptionRequest.getCreatedEndAt())
                .deletedStartAt(billingSearchPaginationOptionRequest.getDeletedStartAt())
                .deletedEndAt(billingSearchPaginationOptionRequest.getDeletedEndAt())
                .build());

        booleanBuilder.and(QApplicationSessionEntity.applicationSessionEntity.id.eq(QBillingEntity.billingEntity.applicationSessionId));

        return booleanBuilder;
    }

    private BooleanBuilder getApplicationPredicate(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest, UUID hostId) {
        BooleanBuilder booleanBuilder = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder()
                .type(billingSearchPaginationOptionRequest.getType())
                .build());

        if (billingSearchPaginationOptionRequest.getApplicationId() != null) {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(Expressions.constant(billingSearchPaginationOptionRequest.getApplicationId().toString())));
        } else {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(QApplicationSessionEntity.applicationSessionEntity.applicationId));
        }

        booleanBuilder.and(QApplicationEntity.applicationEntity.tenantId.eq(Expressions.constant(hostId.toString())));

        return booleanBuilder;
    }

    @NotNull
    private Mono<BillingEntity> saveBilling(ApplicationSessionEntity applicationSession, double cost) {
        Status billingStatus = applicationSession.isDeleted() ? Status.USE : Status.FORCE_STOPPED;
        return billingRepository.findByApplicationSessionId(applicationSession.getId())
                .switchIfEmpty(billingRepository.save(BillingEntity.builder()
                        .applicationSessionId(applicationSession.getId())
                        .status(billingStatus)
                        .cost(cost)
                        .build()))
                .flatMap(billing -> {
                    billing.setStatus(billingStatus);
                    billing.setCost(billing.getCost() + cost);
                    return billingRepository.save(billing);
                });
    }
}
