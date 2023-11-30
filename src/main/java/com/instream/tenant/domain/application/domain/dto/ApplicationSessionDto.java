package com.instream.tenant.domain.application.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;


public record ApplicationSessionDto(
        @Schema(description = "어플리케이션 세션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
        UUID id,

        @Schema(description = "어플리케이션 세션 생성 시간", example = "2023-11-26T02:27:20.492Z")
        LocalDateTime createdAt,

        @Schema(description = "어플리케이션 세션 종료 시간", example = "2023-11-26T02:27:20.492Z")
        LocalDateTime deletedAt
) {
    @Builder
    @QueryProjection
    public ApplicationSessionDto {

    }
}
