package com.instream.tenant.domain.admin.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.admin.admin.domain.dto.AdminDto;
import com.instream.tenant.domain.admin.admin.domain.request.AdminSignInRequest;
import com.instream.tenant.domain.admin.admin.handler.AdminHandler;
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
public class AdminConfig extends RouterConfig {
    private final String v1AdminRoutesTag = "v1-admin-routes";

    @Autowired
    public AdminConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1AdminRoutes(AdminHandler adminHandler) {
        return route().nest(RequestPredicates.path("/v1/admins"),
                builder -> {
                    builder.add(getUserInfo(adminHandler));
                    builder.add(signIn(adminHandler));
                },
                ops -> ops.operationId("v1AdminRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> getUserInfo(AdminHandler adminHandler) {
        return route()
                .GET(
                        "/{adminId}/info",
                        adminHandler::getAdminById,
                        this::buildGetUserInfoSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> signIn(AdminHandler adminHandler) {
        return route()
                .POST(
                        "/sign-in",
                        adminHandler::signIn,
                        this::buildSignInSwagger
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
                .tag(v1AdminRoutesTag)
                .summary("관리자 정보 가져오기 API")
                .description("""
                        관리자 정보를 가져옵니다.
                        """)
                .parameter(parameterBuilder()
                        .name("adminId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003")
                )
                .response(responseBuilder().implementation(AdminDto.class));
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
                .tag(v1AdminRoutesTag)
                .summary("관리자 로그인 API")
                .description("""
                        관리자 로그인합니다.
                        """)
                .requestBody(requestBodyBuilder().implementation(AdminSignInRequest.class))
                .response(responseBuilder().implementation(AdminDto.class));
    }
}
