package com.instream.tenant.domain.billing.domain.request;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SummaryBillingRequest {
    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime createdStartAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime createdEndAt;

    @Schema(description = "종료 날짜 기준 조회 시작 날짜")
    private final LocalDateTime deletedStartAt;

    @Schema(description = "종료 날짜 기준 조회 종료 날짜")
    private final LocalDateTime deletedEndAt;

    public SummaryBillingRequest(LocalDateTime createdStartAt, LocalDateTime createdEndAt, LocalDateTime deletedStartAt, LocalDateTime deletedEndAt) {
        this.createdStartAt = createdStartAt;
        this.createdEndAt = createdEndAt;
        this.deletedStartAt = deletedStartAt;
        this.deletedEndAt = deletedEndAt;
    }

    public static Mono<SummaryBillingRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            LocalDateTime createdStartAt = parseDateTime(queryParams.getFirst("createdStartAt"));
            LocalDateTime createdEndAt = parseDateTime(queryParams.getFirst("createdEndAt"));
            LocalDateTime deletedStartAt = parseDateTime(queryParams.getFirst("deletedStartAt"));
            LocalDateTime deletedEndAt = parseDateTime(queryParams.getFirst("deletedEndAt"));

            SummaryBillingRequest summaryBillingRequest = new SummaryBillingRequest(createdStartAt, createdEndAt, deletedStartAt, deletedEndAt);

            return Mono.just(summaryBillingRequest);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
    }
}
