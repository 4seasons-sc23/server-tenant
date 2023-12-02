package com.instream.tenant.domain.serviceError.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorPatchRequestDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorCreateResponseDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorDetailDto;
import com.instream.tenant.domain.serviceError.handler.ServiceErrorHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ServiceErrorConfig {
    @Bean
    public RouterFunction<ServerResponse> v1ServiceErrorRoutes(
        ServiceErrorHandler serviceErrorHandler) {
        return route().nest(RequestPredicates.path("/v1/errors"),
            builder -> {
                builder.add(getServiceError(serviceErrorHandler));
                builder.add(postServiceError(serviceErrorHandler));
                builder.add(patchServiceError(serviceErrorHandler));
                builder.add(deleteServiceError(serviceErrorHandler));
            },
            ops -> ops.operationId("919")
        ).build();
    }

    private RouterFunction<ServerResponse> getServiceError(ServiceErrorHandler serviceErrorHandler) {
        return route()
            .GET(
                "/{errorId}",
                serviceErrorHandler::getServiceError,
                ops -> ops.operationId("919")
                    .parameter(parameterBuilder().name("errorId").in(ParameterIn.PATH).required(true).example("1"))
                    .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(
                        ServiceErrorDetailDto.class))
            )
            .build();
    }
    private RouterFunction<ServerResponse> postServiceError(ServiceErrorHandler serviceErrorHandler) {
        return route()
            .POST(
                "",
                serviceErrorHandler::postServiceError,
                ops -> ops.operationId("919")
                    .requestBody(requestBodyBuilder().implementation(ServiceErrorCreateRequestDto.class).required(true))
                    .response(responseBuilder().responseCode(HttpStatus.CREATED.name()).implementation(
                        ServiceErrorCreateResponseDto.class))
            )
            .build();
    }

    private RouterFunction<ServerResponse> patchServiceError(ServiceErrorHandler serviceErrorHandler) {
        return route()
            .PATCH(
                "/{errorId}",
                (serviceErrorHandler::patchServiceError),
                ops -> ops.operationId("919")
                    .parameter(parameterBuilder().name("errorId").in(ParameterIn.PATH).required(true).example("1"))
                    .requestBody(requestBodyBuilder().implementation(ServiceErrorPatchRequestDto.class).required(true))
                    .response(responseBuilder().responseCode(HttpStatus.OK.name()).implementation(
                        ServiceErrorCreateResponseDto.class))
            ).build();
    }

    private RouterFunction<ServerResponse> deleteServiceError(ServiceErrorHandler serviceErrorHandler) {
        return route()
            .PATCH(
                "/{errorId}/delete",
                (serviceErrorHandler::deleteServiceError),
                ops -> ops.operationId("919")
                    .parameter(parameterBuilder().name("errorId").in(ParameterIn.PATH).required(true).example("1"))
                    .response(responseBuilder().responseCode(HttpStatus.OK.name()))
            ).build();
    }

}
