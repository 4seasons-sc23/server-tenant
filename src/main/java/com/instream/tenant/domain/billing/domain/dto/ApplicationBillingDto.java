package com.instream.tenant.domain.billing.domain.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
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
public class ApplicationBillingDto {
    @Schema(description = "어플리케이션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private UUID id;

    @Schema(description = "어플리케이션 종류", examples = {"CHAT", "STREAMING"})
    private ApplicationType type;

    @Schema(description = "어플리케이션 상태(N - 활성화, Y - 삭제, P - 비활성화, F - 정지)", examples = {"N", "Y", "P", "F"})
    private Status status;

    @Schema(description = "어플리케이션 생성 시간", example = "2023-11-26T01:59:44.885Z")
    private LocalDateTime createdAt;

    @Schema(description = "조회 기간 동안 어플리케이션의 세션 개수", example = "123")
    private long sessionCount;

    @Schema(description = "어플리케이션 사용요금", example = "123.123123")
    private double cost;

    @QueryProjection
    public ApplicationBillingDto(UUID id, ApplicationType type, Status status, LocalDateTime createdAt,  long sessionCount, double cost) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.sessionCount = sessionCount;
        this.cost = cost;
    }
}
