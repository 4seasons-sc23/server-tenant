package com.instream.tenant.domain.media.config;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import com.instream.tenant.domain.application.domain.request.NginxRtmpStreamEvent;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequestDto;
import com.instream.tenant.domain.media.handler.MediaHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MediaRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1MediaRoutes(MediaHandler mediaHandler) {
        return route().nest(RequestPredicates.path("/v1/medias"),
                builder -> {
                    builder.add(startNginxRtmpStream(mediaHandler));
                    builder.add(endNginxRtmpStream(mediaHandler));
                    builder.add(uploadHlsFiles(mediaHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> startNginxRtmpStream(MediaHandler mediaHandler) {
        return route().POST(
                        "/start",
                        mediaHandler::startNginxRtmpStream,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> endNginxRtmpStream(MediaHandler mediaHandler) {
        return route().POST(
                        "/end",
                        mediaHandler::endNginxRtmpStream,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                )
                .build();
    }

    private RouterFunction<ServerResponse> uploadHlsFiles(MediaHandler mediaHandler) {
        return route().POST(
                        "/hls/upload/{quality}",
                        mediaHandler::uploadMedia,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .requestBody(requestBodyBuilder().implementation(MediaUploadRequestDto.class).required(true).description("Hls Files"))
                )
                .build();
    }
}
