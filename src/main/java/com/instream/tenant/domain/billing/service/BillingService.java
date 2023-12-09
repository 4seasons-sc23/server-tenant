package com.instream.tenant.domain.billing.service;

import com.instream.tenant.domain.admin.billing.domain.dto.AdminBillingDto;
import com.instream.tenant.domain.admin.billing.domain.dto.QAdminBillingDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.QApplicationSessionEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
import com.instream.tenant.domain.application.infra.mapper.ApplicationSessionMapper;
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationQueryBuilder;
import com.instream.tenant.domain.application.model.queryBuilder.ApplicationSessionQueryBuilder;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.billing.domain.dto.*;
import com.instream.tenant.domain.billing.domain.request.*;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.tenant.domain.dto.QTenantDto;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
import com.instream.tenant.domain.tenant.domain.entity.QTenantEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

import static com.querydsl.sql.SQLExpressions.countAll;
import static com.querydsl.sql.SQLExpressions.select;

@Service
@Slf4j
public class BillingService {

    // 10GB 당 0.5$
    private final double MINIO_BILLING_RATIO = 1 * 0.5 / 10737418240L;

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

    public Mono<PaginationDto<CollectionDto<ApplicationBillingDto>>> searchBilling(BillingSearchPaginationOptionRequest paginationOptionRequest, UUID hostId) {
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;
        QApplicationBillingDto qApplicationBillingDto = new QApplicationBillingDto(qApplication.id.as("id"), qApplication.type, qApplication.status, qApplication.createdAt, qApplicationSession.id.count().as("session_count"), qApplicationSession.cost.sum().as("cost"));

        Pageable pageable = paginationOptionRequest.getPageable();
        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(ApplicationBillingSearchPaginationOptionRequest.builder()
                .option(paginationOptionRequest.getOption())
                .build());
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder()
                .type(paginationOptionRequest.getType())
                .build());
        BooleanBuilder applicationPredicateFromBilling = getApplicationPredicate(hostId, null);
        OrderSpecifier[] orderSpecifierArray = applicationSessionQueryBuilder.getOrderSpecifier(paginationOptionRequest);
        Flux<ApplicationBillingDto> applicationBillingDtoFlux;

        applicationPredicate.and(applicationPredicateFromBilling);

        applicationBillingDtoFlux = applicationRepository.query(sqlQuery -> sqlQuery
                        .select(qApplicationBillingDto)
                        .from(qApplication)
                        .where(applicationPredicate)
                        .leftJoin(qApplicationSession).on(applicationSessionPredicate)
                        .groupBy(qApplication.id)
                        .orderBy(orderSpecifierArray)
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset())
                )
                .all()
                .flatMapSequential(Mono::just);

        // TODO: Redis 캐싱 넣기
        if (paginationOptionRequest.isFirstView()) {
            return applicationBillingDtoFlux.collectList()
                    .flatMap(billingDtoList -> applicationSessionRepository.query(sqlQuery -> sqlQuery
                                    .select(qApplicationSession.applicationId.countDistinct())
                                    .from(qApplicationSession)
                                    .where(applicationSessionPredicate)
                                    .join(qApplication).on(applicationPredicate)
                                    .orderBy(orderSpecifierArray)
                            )
                            .one()
                            .flatMap(totalElementCount -> {
                                long pageCount = (long) Math.ceil((double) totalElementCount / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<ApplicationBillingDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(pageCount)
                                        .currentPage(paginationOptionRequest.getPage())
                                        .data(CollectionDto.<ApplicationBillingDto>builder()
                                                .data(billingDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return applicationBillingDtoFlux.collectList()
                .flatMap(billingDtoList -> Mono.just(PaginationDto.<CollectionDto<ApplicationBillingDto>>builder()
                        .currentPage(paginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<ApplicationBillingDto>builder()
                                .data(billingDtoList)
                                .build())
                        .build()));
    }

    public Mono<PaginationDto<CollectionDto<AdminBillingDto>>> searchBillingForAdmin(BillingSearchPaginationOptionRequest paginationOptionRequest) {
        QTenantEntity qTenant = QTenantEntity.tenantEntity;
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        Pageable pageable = paginationOptionRequest.getPageable();
        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(ApplicationBillingSearchPaginationOptionRequest.builder()
                .option(paginationOptionRequest.getOption())
                .build());
        BooleanBuilder tenantPredicate = new BooleanBuilder().and(qTenant.id.eq(qApplication.tenantId));
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder()
                .type(paginationOptionRequest.getType())
                .build());
        OrderSpecifier[] orderSpecifierArray = applicationSessionQueryBuilder.getOrderSpecifier(paginationOptionRequest);
        QAdminBillingDto qAdminBillingDto = new QAdminBillingDto(qTenant.id.as("id"), qTenant.account, qTenant.name, qApplicationSession.cost.sum().coalesce(0.0).as("cost"));
        Function<SQLQuery<?>, SQLQuery<AdminBillingDto>> adminBillingQuery = (sqlQuery) -> sqlQuery
                .select(qAdminBillingDto)
                .from(qTenant)
                .leftJoin(qApplication).on(tenantPredicate)
                .leftJoin(qApplicationSession).on(applicationSessionPredicate)
                .where(applicationPredicate)
                .groupBy(qTenant.id)
                .having(qApplicationSession.cost.sum().coalesce(0.0).gt(0.0));
        Flux<AdminBillingDto> applicationBillingDtoFlux;

        applicationBillingDtoFlux = applicationRepository.query(adminBillingQuery
                        .andThen(adminBillingDtoSQLQuery -> adminBillingDtoSQLQuery
                                .orderBy(orderSpecifierArray)
                                .limit(pageable.getPageSize())
                                .offset(pageable.getOffset())))
                .all()
                .flatMapSequential(adminBillingDto -> Mono.just(AdminBillingDto.builder()
                        .id(adminBillingDto.getId())
                        .account(adminBillingDto.getAccount())
                        .cost(adminBillingDto.getCost())
                        .name(adminBillingDto.getName())
                        .startAt(paginationOptionRequest.getOption().getStartAt())
                        .endAt(paginationOptionRequest.getOption().getEndAt())
                        .build()));

        // TODO: Redis 캐싱 넣기
        if (paginationOptionRequest.isFirstView()) {
            return applicationBillingDtoFlux.collectList()
                    .flatMap(billingDtoList -> applicationSessionRepository.query(adminBillingQuery)
                            .all()
                            .count()
                            .flatMap(totalElementCount -> {
                                long pageCount = (long) Math.ceil((double) totalElementCount / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<AdminBillingDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(pageCount)
                                        .currentPage(paginationOptionRequest.getPage())
                                        .data(CollectionDto.<AdminBillingDto>builder()
                                                .data(billingDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return applicationBillingDtoFlux.collectList()
                .flatMap(billingDtoList -> Mono.just(PaginationDto.<CollectionDto<AdminBillingDto>>builder()
                        .currentPage(paginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<AdminBillingDto>builder()
                                .data(billingDtoList)
                                .build())
                        .build()));
    }

    public Mono<PaginationDto<CollectionDto<BillingDto>>> searchBillingPerApplication(ApplicationBillingSearchPaginationOptionRequest paginationOptionRequest, UUID hostId, UUID applicationId) {
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        Pageable pageable = paginationOptionRequest.getPageable();
        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(paginationOptionRequest);
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder().build());
        BooleanBuilder applicationPredicateFromBilling = getApplicationPredicate(hostId, applicationId);
        OrderSpecifier[] orderSpecifierArray = applicationSessionQueryBuilder.getOrderSpecifier(paginationOptionRequest);
        Flux<BillingDto> billingDtoFlux;

        applicationPredicate.and(applicationPredicateFromBilling);

        billingDtoFlux = applicationSessionRepository.query(sqlQuery -> sqlQuery
                        .select(qApplicationSession)
                        .from(qApplicationSession)
                        .where(applicationSessionPredicate)
                        .leftJoin(qApplication).on(applicationPredicate)
                        .orderBy(orderSpecifierArray)
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset())
                )
                .all()
                .flatMapSequential(applicationSession -> Mono.just(ApplicationSessionMapper.INSTANCE.INSTANCE.entityToBilling(applicationSession)));

        // TODO: Redis 캐싱 넣기
        if (paginationOptionRequest.isFirstView()) {
            return billingDtoFlux.collectList()
                    .flatMap(billingDtoList -> applicationSessionRepository.query(sqlQuery -> sqlQuery
                                    .select(qApplicationSession.id.count())
                                    .from(qApplicationSession)
                                    .where(applicationSessionPredicate)
                                    .join(qApplication).on(applicationPredicate)
                            )
                            .one()
                            .flatMap(totalElementCount -> {
                                long pageCount = (long) Math.ceil((double) totalElementCount / pageable.getPageSize());
                                return Mono.just(PaginationInfoDto.<CollectionDto<BillingDto>>builder()
                                        .totalElementCount(totalElementCount)
                                        .pageCount(pageCount)
                                        .currentPage(paginationOptionRequest.getPage())
                                        .data(CollectionDto.<BillingDto>builder()
                                                .data(billingDtoList)
                                                .build())
                                        .build());
                            }));
        }

        return billingDtoFlux.collectList()
                .flatMap(billingDtoList -> Mono.just(PaginationDto.<CollectionDto<BillingDto>>builder()
                        .currentPage(paginationOptionRequest.getPageable().getPageNumber())
                        .data(CollectionDto.<BillingDto>builder()
                                .data(billingDtoList)
                                .build())
                        .build()));
    }

    public Mono<ServerResponse> getBillingInfo(UUID hostId, UUID billingId) {
        return Mono.empty();
    }

    public Mono<SummaryBillingDto> getBillingSummary(UUID hostId, ApplicationBillingPaginationOption paginationOptionRequest) {
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(ApplicationBillingSearchPaginationOptionRequest.builder()
                .option(paginationOptionRequest)
                .build());
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder().build());
        BooleanBuilder applicationPredicateFromBilling = getApplicationPredicate(hostId, null);

        applicationPredicate.and(applicationPredicateFromBilling);

        return applicationSessionRepository.query(sqlQuery -> sqlQuery
                        .select(qApplicationSession.cost)
                        .from(qApplicationSession)
                        .where(applicationSessionPredicate)
                        .leftJoin(qApplication).on(applicationPredicate)
                )
                .all()
                .reduce(Double::sum)
                .switchIfEmpty(Mono.just(0.0))
                .flatMap(sum -> Mono.just(SummaryBillingDto.builder()
                        .cost(sum == null ? 0.0 : sum)
                        .startAt(paginationOptionRequest.getStartAt())
                        .endAt(paginationOptionRequest.getEndAt())
                        .build()));
    }

    public Mono<SummaryBillingDto> getBillingSummaryForAdmin(ApplicationBillingPaginationOption paginationOptionRequest) {
        QApplicationSessionEntity qApplicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity qApplication = QApplicationEntity.applicationEntity;

        BooleanBuilder applicationSessionPredicate = applicationSessionQueryBuilder.getBillingPredicate(ApplicationBillingSearchPaginationOptionRequest.builder()
                .option(paginationOptionRequest)
                .build());
        BooleanBuilder applicationPredicate = applicationQueryBuilder.getPredicate(ApplicationSearchPaginationOptionRequest.builder().build());
        BooleanBuilder applicationPredicateFromBilling = getApplicationPredicate(null, null);

        applicationPredicate.and(applicationPredicateFromBilling);

        return applicationSessionRepository.query(sqlQuery -> sqlQuery
                        .select(qApplicationSession.cost)
                        .from(qApplicationSession)
                        .where(applicationSessionPredicate)
                        .leftJoin(qApplication).on(applicationPredicate)
                )
                .all()
                .reduce(Double::sum)
                .switchIfEmpty(Mono.just(0.0))
                .flatMap(sum -> Mono.just(SummaryBillingDto.builder()
                        .cost(sum == null ? 0.0 : sum)
                        .startAt(paginationOptionRequest.getStartAt())
                        .endAt(paginationOptionRequest.getEndAt())
                        .build()));
    }

    public Mono<Void> createBilling(CreateBillingRequest createBillingRequest) {
        return applicationSessionRepository.findById(createBillingRequest.sessionId())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> saveBilling(applicationSession, createBillingRequest.count() * CHAT_BILLING_RATIO))
                .then();
    }

    public Mono<Void> createMinioBilling(UUID sessionId, long trafficBytes) {
        return applicationSessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> saveBilling(applicationSession, trafficBytes * MINIO_BILLING_RATIO))
                .then();
    }

    private BooleanBuilder getApplicationPredicate(UUID hostId, UUID applicationId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (applicationId != null) {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(Expressions.constant(applicationId.toString())));
        } else {
            booleanBuilder.and(QApplicationEntity.applicationEntity.id.eq(QApplicationSessionEntity.applicationSessionEntity.applicationId));
        }

        if (hostId != null) {
            booleanBuilder.and(QApplicationEntity.applicationEntity.tenantId.eq(Expressions.constant(hostId.toString())));
        }

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
