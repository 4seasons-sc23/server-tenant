package com.instream.tenant.domain.billing.service;

import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.repository.BillingRepository;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BillingService {
    private final BillingRepository billingRepository;

    @Autowired
    public BillingService(BillingRepository billingRepository) {
        this.billingRepository = billingRepository;
    }

    public Mono<ServerResponse> searchBilling(BillingSearchPaginationOptionRequest billingSearchPaginationOptionRequest, UUID hostId) {
       return Mono.empty();
    }

    public Mono<ServerResponse> getBillingInfo(UUID hostId, UUID billingId) {
        return Mono.empty();
    }
}
