package com.instream.tenant.domain.host.config;

import com.instream.tenant.domain.host.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.host.handler.HostHandler;
import org.springdoc.core.fn.builders.parameter.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Supplier;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1Routes(HostHandler hostHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts"),
                builder -> {
                    builder.add(signUpFunctionSupplier(hostHandler));
                    builder.add(getUserInfoFunctionSupplier(hostHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> signUpFunctionSupplier(HostHandler hostHandler) {
        return route()
                .POST(
                        "/sign-up",
                        hostHandler::createTenant,
                        ops -> ops.operationId("123")
                                .requestBody(requestBodyBuilder().implementation(TenantCreateRequest.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> getUserInfoFunctionSupplier(HostHandler hostHandler) {
        return route()
                .GET("/{hostId}/info", hostHandler::getTenantByAccount, ops -> ops.operationId("123"))
                .build();
    }
}