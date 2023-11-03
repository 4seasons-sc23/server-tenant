package com.instream.tenant.domain.application.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;


public record ApplicationSessionDto(
        String id,

        LocalDateTime createdAt,

        LocalDateTime deletedAt
) {
    @Builder
    public ApplicationSessionDto {

    }
}
