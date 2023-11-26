package com.instream.tenant.domain.tenant.config;

import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.SendMessageParticipantRequest;
import com.instream.tenant.domain.participant.handler.ParticipantHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostParticipantRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1HostParticipantRoutes(ParticipantHandler participantHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts/{hostId}/participants"),
                builder -> {
                    builder.add(perParticipantRoutes(participantHandler));
                    builder.add(searchParticipantSession(participantHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> searchParticipantSession(ParticipantHandler participantHandler) {
        return route()
                .GET(
                        "/histories/join",
                        participantHandler::searchParticipantJoin,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name("hostId")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder()
                                        .name("id")
                                        .in(ParameterIn.PATH)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ApplicationSessionSearchPaginationOptionRequest.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> perParticipantRoutes(ParticipantHandler participantHandler) {
        return route().nest(RequestPredicates.path("/{participantId}"),
                builder -> {
                    builder.add(enterToApplication(participantHandler));
                    builder.add(leaveFromApplication(participantHandler));
                    builder.add(sendMessage(participantHandler));
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> enterToApplication(ParticipantHandler participantHandler) {
        return route()
                .PUT(
                        "/enter",
                        participantHandler::enterToApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("participantId").in(ParameterIn.PATH).required(true).example("123abc"))
                                .requestBody(requestBodyBuilder().implementation(EnterToApplicationParticipantRequest.class).required(true))
                                .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(ParticipantJoinDto.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> leaveFromApplication(ParticipantHandler participantHandler) {
        return route()
                .PATCH(
                        "/leave",
                        participantHandler::leaveFromApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("participantId").in(ParameterIn.PATH).required(true).example("123abc"))
                                .requestBody(requestBodyBuilder().implementation(LeaveFromApplicationParticipantRequest.class).required(true))
                                .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(ParticipantJoinDto.class))
                )
                .build();
    }

    private RouterFunction<ServerResponse> sendMessage(ParticipantHandler participantHandler) {
        return route()
                .POST(
                        "/message",
                        participantHandler::sendMessage,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder()
                                        .name(InstreamHttpHeaders.API_KEY)
                                        .description("API Key")
                                        .in(ParameterIn.HEADER)
                                        .required(true)
                                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("participantId").in(ParameterIn.PATH).required(true).example("123abc"))
                                .requestBody(requestBodyBuilder().implementation(SendMessageParticipantRequest.class).required(true))
                                .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(ParticipantJoinDto.class))
                )
                .build();
    }
}