package com.instream.tenant.domain.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.handler.ApplicationHandler;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
import com.instream.tenant.domain.tenant.domain.request.FindAccountRequestDto;
import com.instream.tenant.domain.tenant.domain.request.FindPasswordRequestDto;
import com.instream.tenant.domain.tenant.domain.request.PatchPasswordRequestDto;
import com.instream.tenant.domain.tenant.domain.request.PatchTenantNameRequestDto;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.tenant.domain.response.FindAccountResponseDto;
import com.instream.tenant.domain.tenant.domain.response.HostWithdrawalResponseDto;
import com.instream.tenant.domain.tenant.handler.HostHandler;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
                    builder.add(findAccount(hostHandler));
                    builder.add(findPassword(hostHandler));
                    builder.add(checkDuplicateAccount(hostHandler));
                    builder.add(withdrawal(hostHandler));
                    builder.add(patchPassword(hostHandler));
                    builder.add(patchHostName(hostHandler));
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

    private RouterFunction<ServerResponse> findAccount(HostHandler hostHandler) {
        return route()
            .POST(
                "/find/id",
                hostHandler::findAccountByPhonenum,
                this::buildFindIdSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> findPassword(HostHandler hostHandler) {
        return route()
            .POST(
                "/find/pw",
                hostHandler::findPasswordByPhonenum,
                this::buildFindPasswordSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> checkDuplicateAccount(HostHandler hostHandler) {
        return route()
            .GET(
                "/duplicate/account",
                hostHandler::checkDuplicateAccount,
                this::buildCheckDuplicateAccountSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> withdrawal(HostHandler hostHandler) {
        return route()
            .PATCH(
                "/{hostId}/withdrawal",
                hostHandler::withdrawal,
                this::buildWithdrawalSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> patchPassword(HostHandler hostHandler) {
        return route()
            .PATCH(
                "/{hostId}/password",
                hostHandler::patchPassword,
                this::buildPatchPasswordSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> patchHostName(HostHandler hostHandler) {
        return route()
            .PATCH(
                "/{hostId}/name",
                hostHandler::patchHostName,
                this::buildPatchHostNameSwagger
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

    private void buildFindIdSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.TENANT_NOT_FOUND
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("findAccount")
            .tag(v1HostRoutesTag)
            .summary("아이디 찾기 API")
            .description("회원가입에 사용한 전화번호를 통해 아이디를 찾습니다.")
            .requestBody(requestBodyBuilder().implementation(FindAccountRequestDto.class))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name())
                .implementation(FindAccountResponseDto.class));
    }

    private void buildFindPasswordSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.TENANT_NOT_FOUND
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("findPassword")
            .tag(v1HostRoutesTag)
            .summary("비밀번호 찾기 API")
            .description("회원가입에 사용한 전화번호를 통해 비밀번호를 재설정합니다.")
            .requestBody(requestBodyBuilder().implementation(FindPasswordRequestDto.class))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }

    private void buildCheckDuplicateAccountSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.EXIST_ACCOUNT
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("checkDuplicateAccount")
            .tag(v1HostRoutesTag)
            .summary("아이디 중복 확인 API")
            .description("회원가입에 사용할 아이디가 중복인지 확인합니다.")
            .parameter(parameterBuilder().name("account"))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }

    private void buildWithdrawalSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.TENANT_NOT_FOUND
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("hostWithdrawal")
            .tag(v1HostRoutesTag)
            .summary("회원 탈퇴 API")
            .description("사용자가 서비스에서 탈퇴합니다.")
            .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true)
                .example("80bd6328-76a7-11ee-b720-0242ac130003"))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name())
                .implementation(HostWithdrawalResponseDto.class));
    }

    private void buildPatchPasswordSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.TENANT_NOT_FOUND,
            TenantErrorCode.UNAUTHORIZED
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("patchPassword")
            .tag(v1HostRoutesTag)
            .summary("비밀번호 변경 API")
            .description("사용자가 사용 중인 비밀번호를 변경합니다.")
            .requestBody(requestBodyBuilder().implementation(PatchPasswordRequestDto.class))
            .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true)
                .example("80bd6328-76a7-11ee-b720-0242ac130003"))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }

    private void buildPatchHostNameSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.BAD_REQUEST,
            TenantErrorCode.TENANT_NOT_FOUND
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("patchHostName")
            .tag(v1HostRoutesTag)
            .summary("테넌트 이름 변경 API")
            .description("테넌트 이름을 변경합니다.")
            .requestBody(requestBodyBuilder().implementation(PatchTenantNameRequestDto.class))
            .parameter(parameterBuilder().name("hostId").in(ParameterIn.PATH).required(true)
                .example("80bd6328-76a7-11ee-b720-0242ac130003"))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name())
                .implementation(TenantDto.class));
    }
}