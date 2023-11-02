package com.instream.tenant.domain.host.handler;

import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.host.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.host.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class HostHandler {
    private final TenantService tenantService;

    @Autowired
    public HostHandler(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public Mono<ServerResponse> getTenantById(ServerRequest request) {
        UUID hostId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return Mono.just(hostId)
                .flatMap(tenantService::getTenantById)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto));
    }

    public Mono<ServerResponse> createTenant(ServerRequest request) {
        return request.bodyToMono(TenantCreateRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tenantService::createTenant)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto));
    }
}
