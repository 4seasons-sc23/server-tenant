package com.instream.tenant.domain.participant.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantDto(
        @NotBlank
        String id,

        @NotBlank
        @JsonIgnore
        UUID tenantId,

        @NotBlank
        String nickname,

        String profileImgUrl,

        LocalDateTime createdAt
) {
        @Builder
        public ParticipantDto {

        }
}
