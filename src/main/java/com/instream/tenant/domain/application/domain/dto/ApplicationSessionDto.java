package com.instream.tenant.domain.application.domain.dto;

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
@NoArgsConstructor
@Builder
public class ApplicationSessionDto {
    @Schema(description = "어플리케이션 세션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private UUID id;

    @Schema(description = "어플리케이션 세션 생성 시간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime createdAt;

    @Schema(description = "어플리케이션 세션 종료 시간", example = "2023-11-26T02:27:20.492Z")
    private LocalDateTime deletedAt;

    @QueryProjection
    public ApplicationSessionDto(UUID id, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
}
