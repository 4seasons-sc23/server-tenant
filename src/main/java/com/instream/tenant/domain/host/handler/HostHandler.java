package com.instream.tenant.domain.host.handler;

import com.instream.tenant.domain.host.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.host.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class HostHandler {
    private final TenantService tenantService;

    @Autowired
    public HostHandler(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public Mono<ServerResponse> hello(ServerRequest request) {
        return ok().body(Mono.just("Hello World!"), String.class);
    }

    public Mono<ServerResponse> getTenantByAccount(ServerRequest request) {
        String account = request.pathVariable("account");
        return tenantService.getTenantByAccount(account)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createTenant(ServerRequest request) {
        return request.bodyToMono(TenantCreateRequest.class)
                .flatMap(tenantService::createTenant)
                .flatMap(tenantDto -> ServerResponse.ok().bodyValue(tenantDto))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }
}
