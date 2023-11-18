package com.instream.tenant.domain.application.config;

import com.instream.tenant.domain.application.domain.request.NginxRtmpStreamEvent;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
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
public class ApplicationRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1ApplicationRoutes(ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/v1/applications"),
                builder -> {
                    builder.add(startApplication(applicationHandler));
                    builder.add(endApplication(applicationHandler));
                    builder.add(deleteApplication(applicationHandler));
                    builder.add(startNginxRtmpStream(applicationHandler));
                    builder.add(endNginxRtmpStream(applicationHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> startNginxRtmpStream(ApplicationHandler applicationHandler) {
        return route()
                .POST(
                        "/start",
                        applicationHandler::startNginxRtmpStream,
                        ops -> ops.operationId("123")
                                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> endNginxRtmpStream(ApplicationHandler applicationHandler) {
        return route()
                .POST(
                        "/end",
                        applicationHandler::endNginxRtmpStream,
                        ops -> ops.operationId("123")
                                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> startApplication(ApplicationHandler applicationHandler) {
        return route()
                .PATCH(
                        "/{applicationId}/start",
                        applicationHandler::startApplication,
                        ops -> ops.operationId("123")
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
        return route()
                .PATCH(
                        "/{applicationId}/end",
                        applicationHandler::endApplication,
                        ops -> ops.operationId("123")
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
                        "/{applicationId}",
                        applicationHandler::deleteApplication,
                        ops -> ops.operationId("123")
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