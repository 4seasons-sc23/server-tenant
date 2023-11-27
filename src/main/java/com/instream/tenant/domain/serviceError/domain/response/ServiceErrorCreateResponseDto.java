package com.instream.tenant.domain.serviceError.domain.response;

import java.util.UUID;
import lombok.Builder;

public record ServiceErrorCreateResponseDto(
    Long errorId,
    UUID tenantId,
    String title,
    String content) {

    @Builder
    public ServiceErrorCreateResponseDto {
        }
}
