package com.instream.tenant.domain.billing.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.dto.QApplicationDto;
import com.instream.tenant.domain.application.domain.dto.QApplicationSessionDto;
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
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.dto.QBillingDto;
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
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
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
    private final BillingRepository billingRepository;

    private final ApplicationSessionRepository applicationSessionRepository;

    private final BillingQueryBuilder billingQueryBuilder;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ApplicationSessionQueryBuilder applicationSessionQueryBuilder;

    @Autowired
    public BillingService(BillingRepository billingRepository, ApplicationSessionRepository applicationSessionRepository, BillingQueryBuilder billingQueryBuilder, ApplicationQueryBuilder applicationQueryBuilder, ApplicationSessionQueryBuilder applicationSessionQueryBuilder) {
        this.billingRepository = billingRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.billingQueryBuilder = billingQueryBuilder;
        this.applicationQueryBuilder = applicationQueryBuilder;
        this.applicationSessionQueryBuilder = applicationSessionQueryBuilder;
    }

    public Mono<PaginationDto<CollectionDto<BillingDto>>> searchBilling(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest, UUID hostId) {
        QBillingEntity billing = QBillingEntity.billingEntity;
        QApplicationSessionEntity applicationSession = QApplicationSessionEntity.applicationSessionEntity;
        QApplicationEntity application = QApplicationEntity.applicationEntity;

        QApplicationSessionDto applicationSessionDto = new QApplicationSessionDto(applicationSession.id, applicationSession.createdAt, applicationSession.deletedAt);
        QApplicationDto applicationDto = new QApplicationDto(application.id, application.type, application.status, application.createdAt, applicationSessionDto);


        Pageable pageable = billingSearchPaginationOptionRequest.getPageable();
        BooleanBuilder billingPredicate = billingQueryBuilder.getBillingPredicate(billingSearchPaginationOptionRequest);
        BooleanBuilder applicationSessionPredicate = getApplicationSessionPredicate(billingSearchPaginationOptionRequest);
        BooleanBuilder applicationPredicate = getApplicationPredicate(billingSearchPaginationOptionRequest, hostId);
        OrderSpecifier[] orderSpecifierArray = billingQueryBuilder.getOrderSpecifier(billingSearchPaginationOptionRequest);

        Flux<BillingDto> billingDtoFlux = billingRepository.query(sqlQuery -> sqlQuery
                .select(Projections.constructor(BillingDto.class,
                        billing.id.as("id"),
                        billing.cost,
                        billing.status,
                        billing.createdAt,
                        billing.updatedAt,
                        Projections.constructor(ApplicationDto.class,
                                application.id,
                                application.type.as("application_type"),
                                application.status.as("application_status"),
                                application.createdAt.as("application_created_at"),
                                Projections.constructor(ApplicationSessionDto.class,
                                        applicationSession.id,
                                        applicationSession.createdAt.as("application_session_created_at"),
                                        applicationSession.deletedAt
                                )
                        )
                ))
                .from(billing)
                .where(billingPredicate)
                .leftJoin(applicationSession).on(applicationSessionPredicate)
                .leftJoin(application).on(applicationPredicate)
                .orderBy(orderSpecifierArray)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
        ).all();


        // TODO: Redis 캐싱 넣기
        if (billingSearchPaginationOptionRequest.isFirstView()) {
            return billingDtoFlux.collectList()
                    .flatMap(billingDtoList -> billingRepository.query(sqlQuery -> sqlQuery
                                    .select(billing.id.count())
                                    .from(billing)
                                    .where(billingPredicate)
                                    .join(applicationSession).on(applicationSessionPredicate)
                                    .join(application).on(applicationPredicate)
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

    public Mono<BillingDto> createBilling(CreateBillingRequest createBillingRequest) {
        return applicationSessionRepository.findById(createBillingRequest.sessionId())
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSession -> saveBilling(createBillingRequest, applicationSession))
                .then(billingRepository.findByApplicationSessionId(createBillingRequest.sessionId()))
                .flatMap(billing -> Mono.just(BillingMapper.INSTANCE.billingAndApplicationToDto(billing, null)));
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
    private Mono<BillingEntity> saveBilling(CreateBillingRequest createBillingRequest, ApplicationSessionEntity applicationSession) {
        Status billingStatus = applicationSession.isDeleted() ? Status.USE : Status.FORCE_STOPPED;
        return billingRepository.findByApplicationSessionId(createBillingRequest.sessionId())
                .switchIfEmpty(billingRepository.save(BillingEntity.builder()
                        .applicationSessionId(createBillingRequest.sessionId())
                        .status(billingStatus)
                        .cost(createBillingRequest.cost())
                        .build()))
                .flatMap(billing -> {
                    billing.setStatus(billingStatus);
                    billing.setCost(createBillingRequest.cost());
                    return billingRepository.save(billing);
                });
    }
}
