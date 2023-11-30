package com.instream.tenant.domain.billing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.request.CreateBillingRequest;
import com.instream.tenant.domain.billing.domain.request.CreateMinioBillingRequest;
import com.instream.tenant.domain.billing.handler.BillingHandler;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
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
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class BillingConfig extends RouterConfig {
    private final String v1BillingRoutesTag = "v1-billing-routes";

    @Autowired
    public BillingConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1BillingRoutes(BillingHandler billingHandler) {
        return route().nest(RequestPredicates.path("/v1/billings"),
                builder -> {
                    builder.add(createBilling(billingHandler));
                    builder.add(createMinioBilling(billingHandler));
                },
                ops -> ops.operationId("v1BillingRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> createBilling(BillingHandler billingHandler) {
        return route()
                .POST(
                        "",
                        billingHandler::createMinioBilling,
                        this::buildCreateBillingSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> createMinioBilling(BillingHandler billingHandler) {
        return route()
                .POST(
                        "/media",
                        billingHandler::createMinioBilling,
                        this::buildCreateBillingMinioSwagger
                )
                .build();
    }

    private void buildCreateBillingSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);


        ops.operationId("createBilling")
                .tag(v1BillingRoutesTag)
                .summary("사용량 생성 및 수정 API")
                .description("""
                        사용량을 생성 또는 수정합니다.
                                                
                        해당 sessionId에 해당하는 사용량이 없다면 생성, 있다면 값을 추가해서 업데이트합니다.
                        """)
                .requestBody(requestBodyBuilder().implementation(CreateBillingRequest.class))
                .response(responseBuilder().responseCode("200"));
    }

    private void buildCreateBillingMinioSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("createMinioBilling")
                .tag(v1BillingRoutesTag)
                .summary("영상 사용량 생성 및 수정 API")
                .description("""
                        영상 사용량을 생성 또는 수정합니다.
                                                
                        해당 sessionId에 해당하는 사용량이 없다면 생성, 있다면 값을 추가해서 업데이트합니다.
                        """)
                .requestBody(requestBodyBuilder().implementation(CreateMinioBillingRequest.class))
                .response(responseBuilder().responseCode("200"));
    }
}
