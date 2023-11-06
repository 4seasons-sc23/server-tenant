package com.instream.tenant.domain.tenant.handler;

import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
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
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return Mono.just(hostId)
                .flatMap(tenantService::getTenantById)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto));
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request.bodyToMono(TenantSignInRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tenantService::signIn)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto));
    }

    public Mono<ServerResponse> signUp(ServerRequest request) {
        return request.bodyToMono(TenantCreateRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tenantService::signUp)
                .flatMap(tenantDto -> ServerResponse.created(URI.create(String.format("/%s/info", tenantDto.id())))
                        .bodyValue(tenantDto));
    }
}
