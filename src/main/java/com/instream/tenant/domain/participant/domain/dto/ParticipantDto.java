package com.instream.tenant.domain.participant.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantDto(
        @NotBlank
        @Schema(description = "참가자 ID", example = "123String123")
        String id,

        @NotBlank
        @JsonIgnore
        @Schema(description = "Tenant ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID tenantId,

        @NotBlank
        @Schema(description = "참가자 닉네임", example = "참가자닉네임")
        String nickname,

        @Schema(description = "참가자 닉네임", example = "https://image.image.com/image.png")
        String profileImgUrl,

        @Schema(description = "참가자 정보 생성 시간", example = "2023-11-27T11:27:15.387Z")
        LocalDateTime createdAt
) {
    @Builder
    public ParticipantDto {

    }
}
