package com.instream.tenant.domain.media.config;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.media.handler.MediaHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class MediaRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1MediaRoutes(MediaHandler mediaHandler) {
        return route().nest(RequestPredicates.path("/v1/medias"),
                builder -> {
                    builder.add(startLive(mediaHandler));
                    builder.add(endLive(mediaHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }
    
    private RouterFunction<ServerResponse> startLive(MediaHandler mediaHandler) {
        return route()
                .PATCH(
                        "/lives/start",
                        mediaHandler::startApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .response(Builder.responseBuilder().implementation(ApplicationSessionDto.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> endLive(MediaHandler mediaHandler) {
        return route()
                .PATCH(
                        "/lives/end",
                        mediaHandler::endApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("applicationId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .response(Builder.responseBuilder().implementation(ApplicationSessionDto.class))
                )
                .build();
    }
}