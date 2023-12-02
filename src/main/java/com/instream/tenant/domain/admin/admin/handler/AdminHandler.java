package com.instream.tenant.domain.admin.admin.handler;

import com.instream.tenant.domain.admin.admin.domain.request.AdminSignInRequest;
import com.instream.tenant.domain.admin.admin.service.AdminService;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class AdminHandler {
    private final AdminService adminService;

    @Autowired
    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public Mono<ServerResponse> getAdminById(ServerRequest request) {
        long adminId;

        try {
            adminId = Long.parseLong(request.pathVariable("adminId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return adminService.getAdminById(adminId)
                .flatMap(adminDto -> ServerResponse.ok().bodyValue(adminDto));
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request.bodyToMono(AdminSignInRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(adminService::signIn)
                .flatMap(adminDto -> ServerResponse.ok().bodyValue(adminDto));
    }
}
