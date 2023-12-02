package com.instream.tenant.domain.billing.domain.dto;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryBillingDto {
    @Schema(description = "사용량 요금", example = "123123.123123")
    private Double cost;

    @Schema(description = "사용량 요악 조회 시작 기간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime startAt;

    @Schema(description = "사용량 요악 조회 종료 기간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime endAt;
}
