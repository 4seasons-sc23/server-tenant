package com.instream.tenant.domain.participant.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantJoinDto(
        @NotNull
        UUID id,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        ParticipantDto participant
) {
        @Builder
        public ParticipantJoinDto {

        }
}
