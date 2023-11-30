package com.instream.tenant.domain.application.domain.dto;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
public class ApplicationDto {
    @Schema(description = "어플리케이션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private UUID id;

    @Schema(description = "어플리케이션 종류", examples = {"CHAT", "STREAMING"})
    private ApplicationType type;

    @Schema(description = "어플리케이션 상태(N - 활성화, Y - 삭제, P - 비활성화, F - 정지)", examples = {"N", "Y", "P", "F"})
    private Status status;

    @Schema(description = "어플리케이션 생성 시간", example = "2023-11-26T01:59:44.885Z")
    private LocalDateTime createdAt;

    @Schema(description = "어플리케이션 세션")
    private ApplicationSessionDto session;

    @QueryProjection
    public ApplicationDto(UUID id, ApplicationType type, Status status, LocalDateTime createdAt, ApplicationSessionDto session) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.session = session;
    }
}
