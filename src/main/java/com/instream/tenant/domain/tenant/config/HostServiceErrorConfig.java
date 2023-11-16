package com.instream.tenant.domain.tenant.config;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.participant.handler.ParticipantHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HostErrorConfig {
    @Bean
    public RouterFunction<ServerResponse> v1HostErrorRoutes(
        ApplicationHandler applicationHandler, ParticipantHandler participantHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts/errors"),
            builder -> {
                builder.add(searchApplication(applicationHandler));
                builder.add(createApplication(applicationHandler));
                builder.add(startApplication(applicationHandler));
                builder.add(endApplication(applicationHandler));
                builder.add(deleteApplication(applicationHandler));
                builder.add(searchApplicationSession(applicationHandler));
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
                    .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(
                        ApplicationSearchPaginationOptionRequest.class))
            )
            .build();
    }

}
