package com.instream.tenant.domain.tenant.config;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.response.ApplicationCreateResponse;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
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
                    builder.add(enterToApplication(participantHandler));
                    builder.add(leaveFromApplication(participantHandler));;
                },
                ops -> ops.operationId("123")
        ).build();
    }

    private RouterFunction<ServerResponse> enterToApplication(ParticipantHandler participantHandler) {
        return route()
                .PUT(
                        "/{participantId}/enter",
                        participantHandler::enterToApplication,
                        ops -> ops.operationId("123")
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
                        "/{participantId}/leave",
                        participantHandler::leaveFromApplication,
                        ops -> ops.operationId("123")
                                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                                .parameter(parameterBuilder().name("participantId").in(ParameterIn.PATH).required(true).example("123abc"))
                                .requestBody(requestBodyBuilder().implementation(LeaveFromApplicationParticipantRequest.class).required(true))
                                .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(ParticipantJoinDto.class))
                )
                .build();
    }
}