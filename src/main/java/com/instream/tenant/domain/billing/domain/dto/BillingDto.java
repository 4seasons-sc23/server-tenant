package com.instream.tenant.domain.billing.domain.dto;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record BillingDto(
        @Schema(description = "사용량 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
        UUID id,

        @Schema(description = "사용량 요금", example = "123123.123123")
        double cost,

        @Schema(description = "사용량 상태(N - 결제 완료, Y - 삭제, P - 결제 대기중, F - 사용량 추적 중(사용자한테 노출 X))", examples = {"N", "Y", "P", "F"})
        Status status,


        @Schema(description = "사용량 생성 시간", example = "2023-11-26T02:27:20.492Z")
        LocalDateTime createdAt,

        @Schema(description = "사용량 수정 시간", example = "2023-11-26T02:27:20.492Z")
        LocalDateTime updatedAt,

        @Schema(description = "어플리케이션")
        ApplicationDto application
) {
    @Builder
    @QueryProjection
    public BillingDto {

    }
}
