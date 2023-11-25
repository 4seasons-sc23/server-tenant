package com.instream.tenant.domain.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
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
    private final String v1ApplicationRoutesTag = "v1-application-routes";

    private final String v1ApplicationSessionsRoutesTag = "v1-application-session-routes";

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
                    builder.add(searchApplicationSession(applicationHandler));
                    builder.add(startApplicationSession(applicationHandler));
                    builder.add(endApplicationSession(applicationHandler));
                },
                ops -> ops.operationId("v1ApplicationSessionsRoutes")
                        .tag(v1ApplicationSessionsRoutesTag)
        ).build();
    }

    private RouterFunction<ServerResponse> searchApplicationSession(ApplicationHandler applicationHandler) {
        return route()
                .GET(
                        "",
                        applicationHandler::searchApplicationSession,
                        this::buildSearchApplicationSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> startApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH("/start",
                applicationHandler::startApplicationSession,
                this::buildStartApplicationSessionSwagger
        ).build();
    }

    private RouterFunction<ServerResponse> endApplicationSession(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplicationSession,
                        this::buildEndApplicationSessionSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> startApplication(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/start",
                        applicationHandler::startApplication,
                        this::buildStartApplicationSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> endApplication(ApplicationHandler applicationHandler) {
        return route().PATCH(
                        "/end",
                        applicationHandler::endApplication,
                        this::buildEndApplicationSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> deleteApplication(ApplicationHandler applicationHandler) {
        return route()
                .DELETE(
                        "",
                        applicationHandler::deleteApplication,
                        this::buildDeleteApplicationSwagger
                )
                .build();
    }

    private void buildHttpErrorResponse(Builder ops, List<HttpErrorCode> httpErrorCodeList) {
        httpErrorCodeList.forEach(httpErrorCode -> ops.response(SwaggerErrorResponseBuilderTemplate.basic.getTemplateFunction().apply(httpErrorCode, objectMapper)));
    }

    private void buildSearchApplicationSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("searchApplicationSwagger")
                .summary("어플리케이션 세션 검색 API")
                .description("""
                        어플리케이션 세션을 검색합니다.
                        
                        정렬은 sort에서 원하는 옵션과 ASC(오름차순), DESC(내림차순)을 입력하시면 됩니다. QueryParameter에서는 sort[name]=createdAt&sort[option]=DESC 와 같은 방식으로 입력해주셔야 처리 가능합니다. Swagger에서 요청하면 예시 url 나오니까 테스트 해보세요.
                        
                        전체 페이지 및 데이터 개수가 필요할 때만 firstView를 true로, 아니라면 false로 호출해주세요. SQL 성능 차이가 납니다.
                        
                        현재 세션 기간별 조회 옵션을 지원합니다.
                        
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
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
                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ApplicationSessionSearchPaginationOptionRequest.class));
    }

    private void buildStartApplicationSessionSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

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

    private void buildEndApplicationSessionSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);


        ops.operationId("endApplicationSession")
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
                .response(responseBuilder()
                        .responseCode("200")
                        .content(contentBuilder().schema(schemaBuilder().implementation(ApplicationSessionDto.class)))
                );
    }

    private void buildStartApplicationSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("startApplication")
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
                .response(responseBuilder()
                        .responseCode("200")
                        .content(contentBuilder().schema(schemaBuilder().implementation(ApplicationDto.class))));
    }

    private void buildEndApplicationSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("endApplication")
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
                .response(responseBuilder()
                        .responseCode("200")
                        .content(contentBuilder().schema(schemaBuilder().implementation(ApplicationDto.class))));
    }

    private void buildDeleteApplicationSwagger(Builder ops) {
        ops.operationId("deleteApplication")
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
                .response(responseBuilder()
                        .responseCode("200"));
    }
}