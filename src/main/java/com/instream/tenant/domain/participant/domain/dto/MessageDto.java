package com.instream.tenant.domain.participant.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDto(
        UUID messageId,
        String message,
        LocalDateTime createdAt
) {
    @Builder
    public MessageDto {

    }
}
