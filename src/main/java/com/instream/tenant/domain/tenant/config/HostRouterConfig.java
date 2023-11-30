package com.instream.tenant.domain.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}