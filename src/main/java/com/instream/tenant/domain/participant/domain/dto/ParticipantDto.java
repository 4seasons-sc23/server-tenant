package com.instream.tenant.domain.participant.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

public record ParticipantDto(
        @NotBlank
        String participantId,

        @NotBlank
        @JsonIgnore
        String tenantId,

        @NotBlank
        String nickname,

        String profileImgUrl,

        LocalDateTime createdAt
) {
        @Builder
        public ParticipantDto {

        }
}
