package com.instream.tenant.domain.tenant.config;

import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.participant.handler.ParticipantHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.UUID;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostApplicationRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1HostApplicationRoutes(ApplicationHandler applicationHandler, ParticipantHandler participantHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts/{hostId}/applications"),
                builder -> {
                    builder.add(searchApplication(applicationHandler));
                    builder.add(createApplication(applicationHandler));
                    builder.add(searchParticipantSession(participantHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> searchApplication(ApplicationHandler applicationHandler) {
        return route()
                .GET(
                        "",
                        applicationHandler::searchApplication,
                        ops -> ops.operationId(String.format("pagination_%s", ApplicationWithApiKeyDto.class.getSimpleName()))
                                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ApplicationSearchPaginationOptionRequest.class))
                                .response(responseBuilder().implementation(ApplicationWithApiKeyDto.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> createApplication(ApplicationHandler applicationHandler) {
        return route()
                .POST(
                        "",
                        applicationHandler::createApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .requestBody(requestBodyBuilder().implementation(ApplicationCreateRequest.class))
                                .response(responseBuilder().implementation(UUID.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> searchParticipantSession(ParticipantHandler participantHandler) {
        return route()
                .GET(
                        "sessions/{sessionId}/participants",
                        participantHandler::searchParticipantJoinWithApplicationSession,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("sessionId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ParticipantJoinSearchPaginationOptionRequest.class))
                )
                .build();
    }
}