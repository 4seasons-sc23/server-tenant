package com.instream.tenant.domain.admin.billing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.admin.billing.handler.AdminBillingHandler;
import com.instream.tenant.domain.billing.domain.dto.ApplicationBillingDto;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.dto.SummaryBillingDto;
import com.instream.tenant.domain.billing.domain.request.ApplicationBillingPaginationOption;
import com.instream.tenant.domain.billing.domain.request.BillingSearchPaginationOptionRequest;
import com.instream.tenant.domain.billing.handler.BillingHandler;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
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
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class AdminBillingRouterConfig extends RouterConfig {
    private final String v1AdminBillingRoutesTag = "v1-admin-billing-routes";

    @Autowired
    public AdminBillingRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1AdminBillingRoutes(AdminBillingHandler adminBillingHandler) {
        return route().nest(RequestPredicates.path("/v1/admins/billings"),
                builder -> {
                    builder.add(searchBilling(adminBillingHandler));
                    builder.add(getBillingSummary(adminBillingHandler));
                },
                ops -> ops.operationId("v1AdminBillingRoutes")
        ).build();
    }

    private RouterFunction<ServerResponse> searchBilling(AdminBillingHandler adminBillingHandler) {
        return route()
                .GET(
                        "",
                        adminBillingHandler::searchBilling,
                        this::buildSearchBillingSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> getBillingSummary(AdminBillingHandler adminBillingHandler) {
        return route()
                .GET(
                        "/summary",
                        adminBillingHandler::getBillingSummary,
                        this::buildGetBillingSummarySwagger
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

        ops.operationId(String.format("pagination_%s", ApplicationBillingDto.class.getSimpleName()))
                .tag(v1AdminBillingRoutesTag)
                .summary("관리자 사용량 내역 검색 API")
                .description("""
                        관리자가 사용량 내역을 검색합니다. startAt & endAt으로 기간 조회를 지원합니다. 해당 값이 없으면 회원가입 이후 전체기간 사용량 요금을 조회합니다.
                                                
                        정렬은 sort에서 원하는 옵션과 ASC(오름차순), DESC(내림차순)을 입력하시면 됩니다. name 항목은 DB 컬럼 기준 CamelCase로 입력받고 있습니다. 자세한 항목은 ErdCloud 참고해주세요.
                                                
                        QueryParameter에서는 sort[name]=createdAt&sort[option]=DESC 와 같은 방식으로 입력해주셔야 처리 가능합니다. Swagger에서 요청하면 Responses 항목에서 예시 url을 보실 수 있습니다.
                                                
                        전체 페이지 및 데이터 개수가 필요할 때만 firstView를 true로, 아니라면 false로 호출해주세요. SQL 성능 차이가 납니다.
                                                
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
                        """)
                .parameter(parameterBuilder().name("option").in(ParameterIn.QUERY).required(true).implementation(BillingSearchPaginationOptionRequest.class))
                .response(responseBuilder().responseCode("200").implementation(ApplicationBillingDto.class));
    }

    private void buildGetBillingSummarySwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                TenantErrorCode.TENANT_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("billingSummary")
                .tag(v1AdminBillingRoutesTag)
                .summary("관리자 사용량 내역 요약 API")
                .description("""
                        관리자 사용량 내역 요약을 검색합니다.
                                                
                        현재 기간별 조회 옵션을 지원합니다.
                                                
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
                        """)
                .parameter(parameterBuilder().name("option").in(ParameterIn.QUERY).required(true).implementation(ApplicationBillingPaginationOption.class))
                .response(responseBuilder().responseCode("200").implementation(SummaryBillingDto.class));
    }
}