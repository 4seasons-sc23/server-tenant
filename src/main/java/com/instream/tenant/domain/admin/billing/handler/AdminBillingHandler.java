package com.instream.tenant.domain.admin.billing.handler;

import com.instream.tenant.domain.billing.domain.request.*;
import com.instream.tenant.domain.billing.service.BillingService;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class AdminBillingHandler {
    private final BillingService billingService;

    @Autowired
    public AdminBillingHandler(BillingService billingService) {
        this.billingService = billingService;
    }

    public Mono<ServerResponse> searchBilling(ServerRequest request) {
        return BillingSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((billingService::searchBillingForAdmin))
                .flatMap(billingPaginationDto -> ServerResponse.ok().bodyValue(billingPaginationDto));
    }

    public Mono<ServerResponse> getBillingSummary(ServerRequest request) {
        return Mono.just(ApplicationBillingPaginationOption.fromQueryParams(request.queryParams()))
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(billingService::getBillingSummaryForAdmin)
                .flatMap(billingDto -> ServerResponse.ok().bodyValue(billingDto));
    }
}
