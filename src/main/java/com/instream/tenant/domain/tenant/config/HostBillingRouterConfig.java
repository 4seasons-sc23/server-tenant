package com.instream.tenant.domain.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.billing.handler.BillingHandler;
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
public class HostBillingRouterConfig extends RouterConfig {
    private final String v1HostBillingRoutesTag = "v1-host-billing-routes";

    @Autowired
    public HostBillingRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1HostBillingRoutes(BillingHandler billingHandler) {
        return route().nest(RequestPredicates.path("/v1/hosts/{hostId}/billings"),
                builder -> {
                    builder.add(searchBilling(billingHandler));
                    builder.add(getBillingInfo(billingHandler));
                },
                ops -> ops.operationId("v1HostBillingRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> searchBilling(BillingHandler billingHandler) {
        return route()
                .GET(
                        "",
                        billingHandler::searchBilling,
                        this::buildSearchBillingSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> getBillingInfo(BillingHandler billingHandler) {
        return route()
                .POST(
                        "/{billingId}",
                        billingHandler::getBillingInfo,
                        this::buildGetBillingInfoSwagger
                )
                .build();
    }

    private void buildSearchBillingSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("searchBilling")
                .tag(v1HostBillingRoutesTag)
                .summary("Tenant 사용량 내역 검색 API")
                .description("""
                        Tenant의 사용량 내역을 검색합니다.
                                                
                        정렬은 sort에서 원하는 옵션과 ASC(오름차순), DESC(내림차순)을 입력하시면 됩니다. name 항목은 DB 컬럼 기준 CamelCase로 입력받고 있습니다. 자세한 항목은 ErdCloud 참고해주세요.
                                                
                        QueryParameter에서는 sort[name]=createdAt&sort[option]=DESC 와 같은 방식으로 입력해주셔야 처리 가능합니다. Swagger에서 요청하면 Responses 항목에서 예시 url을 보실 수 있습니다.
                                                
                        전체 페이지 및 데이터 개수가 필요할 때만 firstView를 true로, 아니라면 false로 호출해주세요. SQL 성능 차이가 납니다.
                                                
                        현재 세션 기간별 조회 옵션을 지원합니다.
                                                
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
                        """)
                .parameter(parameterBuilder()
                        .name("hostId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .response(responseBuilder().responseCode("200").implementation(TenantDto.class));
    }

    private void buildGetBillingInfoSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);


        ops.operationId("getBillingInfo")
                .tag(v1HostBillingRoutesTag)
                .summary("사용량 상세 내역 정보 가져오기 API")
                .description("""
                        사용량 상세 내역 정보를 가져옵니다.
                        """)
                .parameter(parameterBuilder()
                        .name("hostId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .parameter(parameterBuilder()
                        .name("billingId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .response(responseBuilder().implementation(TenantDto.class));
    }
}