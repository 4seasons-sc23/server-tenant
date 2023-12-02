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
    private TenantDto tenant;

    private SummaryBillingDto summaryBilling;

    @QueryProjection
    public AdminBillingDto(UUID id, String account, String name, String phoneNumber, Status status, String session, Double cost, LocalDateTime startAt, LocalDateTime endAt) {
        this.tenant = new TenantDto(id, account, name, phoneNumber, status, session);
        this.summaryBilling = new SummaryBillingDto(cost, startAt, endAt);
    }
}
