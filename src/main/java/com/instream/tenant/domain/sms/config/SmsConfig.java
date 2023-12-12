package com.instream.tenant.domain.sms.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.sms.domain.requests.AuthNumberRequestDto;
import com.instream.tenant.domain.sms.handler.SmsHandler;
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
            },
            ops -> ops.operationId("v1HostRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> sendAuthNumber(SmsHandler smsHandler) {
        return route()
            .POST(
                "/authNum",
                smsHandler::sendAuthNumber,
                this::buildSendAuthNumberSwagger
            )
            .build();
    }

    private void buildSendAuthNumberSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
            CommonHttpErrorCode.UNAUTHORIZED,
            CommonHttpErrorCode.BAD_REQUEST
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("signUp")
            .tag(v1SmsRoutesTag)
            .summary("인증번호 요청 API")
            .description("""
                        해당 전화번호로 인증번호 발송을 요청합니다.
                        """)
            .requestBody(requestBodyBuilder().implementation(AuthNumberRequestDto.class))
            .response(responseBuilder().responseCode("200").description(HttpStatus.OK.name()));
    }
}
