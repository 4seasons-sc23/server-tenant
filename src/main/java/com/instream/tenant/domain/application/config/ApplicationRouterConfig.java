package com.instream.tenant.domain.application.config;

import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.application.infra.enums.ApplicationSwaggerOperationId;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.UUID;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class ApplicationRouterConfig {
    private final String v1ApplicationRoutesTag = "v1ApplicationRoutes";

    private final String v1ApplicationSessionsRoutesTag = "v1ApplicationSessionsRoutes";

    @Bean
    public RouterFunction<ServerResponse> v1ApplicationRoutes(ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/v1/applications/{applicationId}"),
                builder -> {
                    builder.add(startApplication(applicationHandler));
                    builder.add(endApplication(applicationHandler));
                    builder.add(deleteApplication(applicationHandler));
                    builder.add(v1ApplicationSessionsRoutes(applicationHandler));
                },
                ops -> ops.operationId("v1ApplicationRoutes")
                        .tag(v1ApplicationRoutesTag)
                        .parameter(parameterBuilder()
                                .name("applicationId")
                                .in(ParameterIn.PATH)
                                .required(true)
                                .example("80bd6328-76a7-11ee-b720-0242ac130003"))
        ).build();
    }

    public RouterFunction<ServerResponse> v1ApplicationSessionsRoutes(ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/sessions"),
                builder -> {
                    builder.add(startApplicationSession(applicationHandler));
                    builder.add(endApplicationSession(applicationHandler));
                },
                ops -> ops.operationId("v1ApplicationSessionsRoutes")
                        .tag(v1ApplicationSessionsRoutesTag)
        ).build();
    }


    private RouterFunction<ServerResponse> startApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/start",
                        applicationHandler::startApplication,
                        ops -> ops.operationId("startApplicationSession")
                                .tag(v1ApplicationSessionsRoutesTag)
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> endApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplication,
                        ops -> ops.operationId("endApplicationSession")
                                .tag(v1ApplicationSessionsRoutesTag)
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> startApplication(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/start",
                        applicationHandler::startApplication,
                        ops -> ops.operationId("startApplication")
                                .tag(v1ApplicationRoutesTag)
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> endApplication(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplication,
                        ops -> ops.operationId("endApplication")
                                .tag(v1ApplicationRoutesTag)
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> deleteApplication(ApplicationHandler applicationHandler) {
        return route()
                .DELETE(
                        "",
                        applicationHandler::deleteApplication,
                        ops -> ops.operationId("deleteApplication")
                                .tag(v1ApplicationRoutesTag)
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                )
                .build();
    }
}