package com.instream.tenant.domain.participant.domain.dto;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantJoinDto(
        @NotNull
        UUID id,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        ParticipantDto participant,

        ApplicationDto application
) {
        @Builder
        public ParticipantJoinDto {

        }
}
