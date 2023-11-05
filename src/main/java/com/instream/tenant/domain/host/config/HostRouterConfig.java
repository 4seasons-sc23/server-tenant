package com.instream.tenant.domain.host.config;

import com.instream.tenant.domain.host.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.host.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.host.handler.HostHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1Routes(HostHandler hostHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts"),
                builder -> {
                    builder.add(getUserInfoFunctionSupplier(hostHandler));
                    builder.add(signInFunctionSupplier(hostHandler));
                    builder.add(signUpFunctionSupplier(hostHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> getUserInfoFunctionSupplier(HostHandler hostHandler) {
        return route()
                .GET(
                        "/{hostId}/info",
                        hostHandler::getTenantById,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003")
                                )
                )
                .build();
    }

    private RouterFunction<ServerResponse> signInFunctionSupplier(HostHandler hostHandler) {
        return route()
                .POST(
                        "/sign-in",
                        hostHandler::signIn,
                        ops -> ops.operationId("123")
                                .requestBody(requestBodyBuilder().implementation(TenantSignInRequest.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> signUpFunctionSupplier(HostHandler hostHandler) {
        return route()
                .POST(
                        "/sign-up",
                        hostHandler::signUp,
                        ops -> ops.operationId("123")
                                .requestBody(requestBodyBuilder().implementation(TenantCreateRequest.class))
                )
                .build();
    }


}