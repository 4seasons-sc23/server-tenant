package com.instream.tenant.domain.billing.domain.request;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SummaryBillingRequest {
    @Schema(description = "조회 시작 날짜")
    private final LocalDateTime startAt;

    @Schema(description = "조회 종료 날짜")
    private final LocalDateTime endAt;

    public SummaryBillingRequest(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Mono<SummaryBillingRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            LocalDateTime startAt = parseDateTime(queryParams.getFirst("startAt"));
            LocalDateTime endAt = parseDateTime(queryParams.getFirst("endAt"));

            SummaryBillingRequest summaryBillingRequest = new SummaryBillingRequest(startAt, endAt);

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
