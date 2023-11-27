package com.instream.tenant.domain.participant.domain.dto;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantJoinDto(
        @NotNull
        @Schema(description = "참가자 세션 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "참가자 세션 생성 시간", example = "2023-11-27T11:27:15.387Z")
        LocalDateTime createdAt,

        @Schema(description = "참가자 세션 종료 시간", example = "2023-11-27T11:27:15.387Z")
        LocalDateTime updatedAt,

        @Schema(description = "참가자 정보")
        ParticipantDto participant,

        @Schema(description = "어플리케이션 정보")
        ApplicationDto application
) {
    @Builder
    public ParticipantJoinDto {

    }
}
