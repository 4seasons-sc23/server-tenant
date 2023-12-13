package com.instream.tenant.domain.sms.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.sms.domain.requests.AuthNumberRequestDto;
import com.instream.tenant.domain.sms.domain.requests.VerifyAuthNumberRequestDto;
import com.instream.tenant.domain.sms.handler.SmsHandler;
import com.instream.tenant.domain.sms.infra.enums.SmsErrorCode;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SmsConfig  extends RouterConfig {
    private final String v1SmsRoutesTag = "v1-sms-routes";

    public SmsConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1SmsRoutes(SmsHandler smsHandler) {
        return route().nest(RequestPredicates.path("/v1/sms"),
            builder -> {
                builder.add(sendAuthNumber(smsHandler));
                builder.add(verifyAuthNumber(smsHandler));
            },
            ops -> ops.operationId("v1HostRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> sendAuthNumber(SmsHandler smsHandler) {
        return route()
            .POST(
                "/auth",
                smsHandler::sendAuthNumber,
                this::buildSendAuthNumberSwagger
            )
            .build();
    }

    private RouterFunction<ServerResponse> verifyAuthNumber(SmsHandler smsHandler) {
        return route()
            .POST(
                "/auth/verify",
                smsHandler::verifyAuthNumber,
                this::buildVerifyAuthNumberSwagger
            )
            .build();
    }
    private void buildSendAuthNumberSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.UNAUTHORIZED,
            CommonHttpErrorCode.BAD_REQUEST,
            SmsErrorCode.SMS_SEND_ERROR,
            SmsErrorCode.MAKE_SIGNATURE_TYPE_ERROR,
            TenantErrorCode.USER_PHONE_NUM_FORMAT_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("sendAuthNumber")
            .tag(v1SmsRoutesTag)
            .summary("인증번호 요청 API")
            .description("""
                        해당 전화번호로 인증번호 발송을 요청합니다.
                        """)
            .requestBody(requestBodyBuilder().implementation(AuthNumberRequestDto.class))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }

    private void buildVerifyAuthNumberSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.UNAUTHORIZED,
            CommonHttpErrorCode.BAD_REQUEST,
            SmsErrorCode.AUTH_NUMBER_NOT_FOUND,
            SmsErrorCode.AUTH_NUMBER_NOT_MATCH
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("verifyAuthNumber")
            .tag(v1SmsRoutesTag)
            .summary("인증번호 확인 API")
            .description("""
                        사용자가 입력한 인증번호와 sms로 전송한 인증번호가 일치하는지 확인합니다.
                        """)
            .requestBody(requestBodyBuilder().implementation(VerifyAuthNumberRequestDto.class))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }
}
