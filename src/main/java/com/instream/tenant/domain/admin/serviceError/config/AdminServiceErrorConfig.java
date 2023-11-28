package com.instream.tenant.domain.admin.serviceError.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.instream.tenant.domain.admin.serviceError.domain.request.ServiceErrorAnswerRequestDto;
import com.instream.tenant.domain.admin.serviceError.handler.AdminServiceErrorHandler;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorAnswerDto;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AdminServiceErrorConfig {
    @Bean
    public RouterFunction<ServerResponse> v1AdminServiceErrorRoutes(
        AdminServiceErrorHandler adminServiceErrorHandler) {
        return route().nest(RequestPredicates.path("/v1/admins/errors"),
            builder -> {
                builder.add(postServiceErrorAnswer(adminServiceErrorHandler));
                builder.add(patchServiceErrorAnswer(adminServiceErrorHandler));
            },
            ops -> ops.operationId("919")
        ).build();
    }

    private RouterFunction<ServerResponse> postServiceErrorAnswer(AdminServiceErrorHandler adminServiceErrorHandler) {
        return route()
            .POST(
                "/{errorId}/answer",
                adminServiceErrorHandler::postServiceErrorAnswer,
                ops->ops.operationId("919")
                    .parameter(parameterBuilder().name("errorId").in(ParameterIn.PATH).required(true).example("1"))
                    .requestBody(requestBodyBuilder().implementation(ServiceErrorAnswerRequestDto.class).required(true))
                    .response(responseBuilder().responseCode(HttpStatus.CREATED.name()).implementation(
                        ServiceErrorAnswerDto.class))
            )
            .build();
    }

    private RouterFunction<ServerResponse> patchServiceErrorAnswer(AdminServiceErrorHandler adminServiceErrorHandler) {
        return route()
            .PATCH(
                "/{errorId}/answer",
                adminServiceErrorHandler::patchServiceErrorAnswer,
                ops->ops.operationId("919")
                    .parameter(parameterBuilder().name("errorId").in(ParameterIn.PATH).required(true).example("1"))
                    .requestBody(requestBodyBuilder().implementation(ServiceErrorAnswerRequestDto.class).required(true))
                    .response(responseBuilder().responseCode(HttpStatus.CREATED.name()).implementation(
                        ServiceErrorAnswerDto.class))
            )
            .build();
    }

}


