package com.instream.tenant.domain.billing.handler;

import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.domain.request.CreateBillingRequest;
import com.instream.tenant.domain.billing.domain.request.CreateMinioBillingRequest;
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
public class BillingHandler {
    private final BillingService billingService;

    @Autowired
    public BillingHandler(BillingService billingService) {
        this.billingService = billingService;
    }

    public Mono<ServerResponse> searchBilling(ServerRequest request) {
        UUID hostId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return BillingSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((billingSearchPaginationOptionRequest -> billingService.searchBilling(billingSearchPaginationOptionRequest, hostId)))
                .flatMap(billingPaginationDto -> ServerResponse.ok().bodyValue(billingPaginationDto));
    }

    public Mono<ServerResponse> getBillingInfo(ServerRequest request) {
        UUID hostId;
        UUID billingId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            billingId = UUID.fromString(request.pathVariable("billingId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return billingService.getBillingInfo(hostId, billingId)
                .flatMap(billingDto -> ServerResponse.ok().bodyValue(billingDto));
    }

    public Mono<ServerResponse> createBilling(ServerRequest request) {
        return request.bodyToMono(CreateBillingRequest.class)
                .flatMap(billingService::createBilling)
                .then(ServerResponse.ok().build());
    }

    public Mono<ServerResponse> createMinioBilling(ServerRequest request) {
        return request.bodyToMono(CreateMinioBillingRequest.class)
                .flatMap(createMinioBillingRequest -> {
                    String[] paths = createMinioBillingRequest.getKey().split("/", 3);
                    return billingService.createMinioBilling(UUID.fromString(paths[1]));
                })
                .then(ServerResponse.ok().build());
    }
}
