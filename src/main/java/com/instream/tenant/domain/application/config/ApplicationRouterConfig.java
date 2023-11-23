package com.instream.tenant.domain.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.common.infra.enums.SwaggerErrorResponseBuilderTemplate;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class ApplicationRouterConfig {
    private final String v1ApplicationRoutesTag = "v1ApplicationRoutes";

    private final String v1ApplicationSessionsRoutesTag = "v1ApplicationSessionsRoutes";

    private final ObjectMapper objectMapper;

    @Autowired
    public ApplicationRouterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RouterFunction<ServerResponse> v1ApplicationRoutes(ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/v1/applications/{applicationId}"),
                builder -> {
                    builder.add(startApplication(applicationHandler));
                    builder.add(endApplication(applicationHandler));
                    builder.add(deleteApplication(applicationHandler));
                    builder.add(v1ApplicationSessionsRoutes(applicationHandler));
                },
                ops -> ops.operationId("v1ApplicationRoutes")
                        .tag(v1ApplicationRoutesTag)
        ).build();
    }

    public RouterFunction<ServerResponse> v1ApplicationSessionsRoutes(ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/sessions"),
                builder -> {
                    builder.add(startApplicationSession(applicationHandler));
                    builder.add(endApplicationSession(applicationHandler));
                },
                ops -> ops.operationId("v1ApplicationSessionsRoutes")
                        .tag(v1ApplicationSessionsRoutesTag)
        ).build();
    }


    private RouterFunction<ServerResponse> startApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH("/start",
                applicationHandler::startApplicationSession,
                this::getStartApplicationSessionSwagger
        ).build();
    }

    private RouterFunction<ServerResponse> endApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplicationSession,
                        ops -> ops.operationId("endApplicationSession")
                                .summary("어플리케이션 세션 종료 API")
                                .description("""
                                        어플리케이션 세션을 종료합니다. 어플리케이션이 활성화되어 있어야 합니다.
                                        라이브 스트리밍 어플리케이션은 지원하지 않습니다. <POST> /v1/medias/end를 호출해주세요.
                                        """)
                                .tag(v1ApplicationSessionsRoutesTag)
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

    private RouterFunction<ServerResponse> startApplication(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/start",
                        applicationHandler::startApplication,
                        ops -> ops.operationId("startApplication")
                                .summary("어플리케이션 활성화 API")
                                .description("""
                                        어플리케이션을 활성화합니다. 관리자에 의해 해당 어플리케이션이 정지된 경우에는 활성화되지 않습니다.
                                        """)
                                .tag(v1ApplicationRoutesTag)
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
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplication,
                        ops -> ops.operationId("endApplication")
                                .summary("어플리케이션 비활성화 API")
                                .description("""
                                        어플리케이션을 비활성화합니다. 관리자에 의해 해당 어플리케이션이 정지된 경우에는 활성화되지 않습니다.
                                        """)
                                .tag(v1ApplicationRoutesTag)
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
                        "",
                        applicationHandler::deleteApplication,
                        ops -> ops.operationId("deleteApplication")
                                .summary("어플리케이션 삭제 API")
                                .description("""
                                        어플리케이션을 삭제합니다. 관리자에 의해 해당 어플리케이션이 정지된 경우에는 활성화되지 않습니다.
                                        """)
                                .tag(v1ApplicationRoutesTag)
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

    private void getStartApplicationSessionSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.SERVICE_UNAVAILABLE
        ));

        httpErrorCodeList.forEach(httpErrorCode -> ops.response(SwaggerErrorResponseBuilderTemplate.basic.getTemplateFunction().apply(httpErrorCode, objectMapper)));

        ops.operationId("startApplicationSession")
                .summary("어플리케이션 세션 시작 API")
                .description("""
                        어플리케이션 세션을 시작합니다. 어플리케이션이 활성화되어 있어야 합니다.
                        라이브 스트리밍 어플리케이션은 지원하지 않습니다. <POST> /v1/medias/start를 호출해주세요.
                        """)
                .tag(v1ApplicationSessionsRoutesTag)
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
                .response(responseBuilder()
                        .responseCode("200")
                        .content(contentBuilder().schema(schemaBuilder().implementation(ApplicationSessionDto.class)))
                );
    }

}