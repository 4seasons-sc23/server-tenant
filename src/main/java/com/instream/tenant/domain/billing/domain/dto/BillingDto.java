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
public class BillingDto {
    @Schema(description = "사용량 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
   private UUID id;

    @Schema(description = "사용량 요금", example = "123123.123123")
    private double cost;

    @Schema(description = "사용량 상태(N - 결제 완료, Y - 삭제, P - 결제 대기중, F - 사용량 추적 중(사용자한테 노출 X))", examples = {"N", "Y", "P", "F"})
    private Status status;


    @Schema(description = "사용량 생성 시간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime createdAt;

    @Schema(description = "사용량 수정 시간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime updatedAt;

    @Schema(description = "어플리케이션")
    private ApplicationDto application;

    @QueryProjection
    public BillingDto(UUID id, double cost, Status status, LocalDateTime createdAt, LocalDateTime updatedAt, ApplicationDto application) {
        this.id = id;
        this.cost = cost;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.application = application;
    }
}
