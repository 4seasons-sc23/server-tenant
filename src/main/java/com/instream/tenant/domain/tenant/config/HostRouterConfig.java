package com.instream.tenant.domain.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.tenant.handler.HostHandler;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
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
import java.util.UUID;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostRouterConfig extends RouterConfig {
    private final String v1HostRoutesTag = "v1-host-routes";

    @Autowired
    public HostRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1HostRoutes(HostHandler hostHandler, ApplicationHandler applicationHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts"),
                builder -> {
                    builder.add(getUserInfo(hostHandler));
                    builder.add(signIn(hostHandler));
                    builder.add(signUp(hostHandler));
                    builder.add(searchApplication(applicationHandler));
                    builder.add(createApplication(applicationHandler));
                },
                ops -> ops.operationId("v1HostRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> getUserInfo(HostHandler hostHandler) {
        return route()
                .GET(
                        "/{hostId}/info",
                        hostHandler::getTenantById,
                        this::buildGetUserInfoSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> signIn(HostHandler hostHandler) {
        return route()
                .POST(
                        "/sign-in",
                        hostHandler::signIn,
                        this::buildSignInSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> signUp(HostHandler hostHandler) {
        return route()
                .POST(
                        "/sign-up",
                        hostHandler::signUp,
                        this::buildSignUpSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> searchApplication(ApplicationHandler applicationHandler) {
        return route()
                .GET(
                        "/{hostId}/applications",
                        applicationHandler::searchApplication,
                        this::buildSearchApplicationSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> createApplication(ApplicationHandler applicationHandler) {
        return route()
                .POST(
                        "/{hostId}/applications",
                        applicationHandler::createApplication,
                        this::buildCreateApplicationSwagger
                )
                .build();
    }

    private void buildGetUserInfoSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("getUserInfo")
                .tag(v1HostRoutesTag)
                .summary("Tenant 정보 가져오기 API")
                .description("""
                        Tenant의 정보를 가져옵니다.
                        """)
                .parameter(parameterBuilder()
                        .name("hostId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003")
                )
                .response(responseBuilder().implementation(TenantDto.class));
    }

    private void buildSignInSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);


        ops.operationId("signIn")
                .tag(v1HostRoutesTag)
                .summary("로그인 API")
                .description("""
                        로그인합니다.
                        """)
                .requestBody(requestBodyBuilder().implementation(TenantSignInRequest.class))
                .response(responseBuilder().implementation(TenantDto.class));
    }

    private void buildSignUpSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("signUp")
                .tag(v1HostRoutesTag)
                .summary("회원가입 API")
                .description("""
                        회원가입합니다.
                        """)
                .requestBody(requestBodyBuilder().implementation(TenantCreateRequest.class))
                .response(responseBuilder().implementation(TenantDto.class));
    }

    private void buildSearchApplicationSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId(String.format("pagination_%s", ApplicationWithApiKeyDto.class.getSimpleName()))
                .tag(v1HostRoutesTag)
                .summary("Tenant의 어플리케이션 검색 API")
                .description("""
                        Tenant의 어플리케이션을 검색합니다.
                                                
                        정렬은 sort에서 원하는 옵션과 ASC(오름차순), DESC(내림차순)을 입력하시면 됩니다. name 항목은 DB 컬럼 기준 CamelCase로 입력받고 있습니다. 자세한 항목은 ErdCloud 참고해주세요.
                                                
                        QueryParameter에서는 sort[name]=createdAt&sort[option]=DESC 와 같은 방식으로 입력해주셔야 처리 가능합니다. Swagger에서 요청하면 Responses 항목에서 예시 url을 보실 수 있습니다.
                                                
                        전체 페이지 및 데이터 개수가 필요할 때만 firstView를 true로, 아니라면 false로 호출해주세요. SQL 성능 차이가 납니다.
                                                
                        현재 세션 기간별 조회 옵션을 지원합니다.
                                                
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
                        """)
                .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true).example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ApplicationSearchPaginationOptionRequest.class))
                .response(responseBuilder().responseCode("200").implementation(ApplicationWithApiKeyDto.class));
    }

    private void buildCreateApplicationSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("createApplication")
                .tag(v1HostRoutesTag)
                .summary("Tenant의 어플리케이션 생성 API")
                .description("""
                        어플리케이션을 생성합니다. 지원하는 타입은 CHAT, STREAMING 입니다.
                        """)
                .parameter(parameterBuilder()
                        .name("hostId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .requestBody(requestBodyBuilder().implementation(ApplicationCreateRequest.class))
                .response(responseBuilder().responseCode("201").implementation(ApplicationWithApiKeyDto.class));
    }
}