package com.instream.tenant.domain.admin.billing.domain.dto;

import com.instream.tenant.domain.billing.domain.dto.SummaryBillingDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
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
public class AdminBillingDto {
    @Schema(description = "사용량 요금", example = "123123.123123")
    private UUID id;

    @Schema(description = "Tenant 계정", example = "testAccount")
    private String account;

    @Schema(description = "Tenant 이름", example = "랄라")
    private String name;

    @Schema(description = "사용량 요금", example = "123123.123123")
    private Double cost;

    @Schema(description = "사용량 요악 조회 시작 기간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime startAt;

    @Schema(description = "사용량 요악 조회 종료 기간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime endAt;


    @QueryProjection
    public AdminBillingDto(UUID id, String account, String name, Double cost) {
        this.id = id;
        this.account = account;
        this.name = name;
        this.cost = cost;
    }
}
