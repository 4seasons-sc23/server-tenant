package com.instream.tenant.domain.application.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;


public record ApplicationSessionDto(
        UUID id,

        LocalDateTime createdAt,

        LocalDateTime deletedAt
) {
    @Builder
    public ApplicationSessionDto {

    }
}
