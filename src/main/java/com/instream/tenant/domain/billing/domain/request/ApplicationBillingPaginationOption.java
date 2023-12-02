package com.instream.tenant.domain.billing.domain.request;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Getter
@ToString(callSuper = true)
@AllArgsConstructor
public class ApplicationBillingPaginationOption {
    @Schema(description = "사용량 상태")
    private final Status status;

    @Schema(description = "조회 시작 날짜")
    private final LocalDateTime startAt;

    @Schema(description = "조회 종료 날짜")
    private final LocalDateTime endAt;

    public static ApplicationBillingPaginationOption fromQueryParams(MultiValueMap<String, String> queryParams) {
        Status status = Optional.ofNullable(queryParams.getFirst("status"))
                .map(Status::fromCode)
                .orElse(null);
        boolean invalidParameter = status != null && (status.equals(Status.FORCE_STOPPED) || status.equals(Status.DELETED));

        if (invalidParameter) {
            throw new IllegalArgumentException();
        }

        LocalDateTime startAt = parseDateTime(queryParams.getFirst("startAt"));
        LocalDateTime endAt = parseDateTime(queryParams.getFirst("endAt"));

        return new ApplicationBillingPaginationOption(status, startAt, endAt);
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
    }
}