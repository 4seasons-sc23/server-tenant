package com.instream.tenant.domain.tenant.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorCreateResponseDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorDetailDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorQuestionDto;
import com.instream.tenant.domain.serviceError.handler.ServiceErrorHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HostServiceErrorConfig {
    @Bean
    public RouterFunction<ServerResponse> v1HostServiceErrorRoutes(
        ServiceErrorHandler serviceErrorHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts/{hostId}/errors"),
            builder -> {
                builder.add(getServiceErrorsByHostId(serviceErrorHandler));
            },
            ops -> ops.operationId("919")
        ).build();
    }

    private RouterFunction<ServerResponse> getServiceErrorsByHostId(ServiceErrorHandler serviceErrorHandler) {
        return route()
            .GET(
                "",
                serviceErrorHandler::getServiceErrorsByHostId,
                ops -> ops.operationId("919")
                    .parameter(
                        parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true)
                            .example("14d38654-89cb-11ee-9aae-0242ac140002"))
                    .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option")
                        .implementation(PaginationOptionRequest.class))
                    .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(
                        ServiceErrorQuestionDto.class))
            )
            .build();
    }
}
