package com.instream.tenant.domain.billing.domain.request;

import com.instream.tenant.domain.common.infra.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
@AllArgsConstructor
public class ApplicationBillingPaginationOption {
    @Schema(description = "사용량 상태")
    private final Status status;

    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime createdStartAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime createdEndAt;

    @Schema(description = "종료 날짜 기준 조회 시작 날짜")
    private final LocalDateTime deletedStartAt;

    @Schema(description = "종료 날짜 기준 조회 종료 날짜")
    private final LocalDateTime deletedEndAt;
}
