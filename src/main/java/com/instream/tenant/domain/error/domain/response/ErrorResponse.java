package com.instream.tenant.domain.error.domain.response;

import lombok.Builder;

public record ErrorResponse(
        String code,
        String message
) {
    @Builder
    public ErrorResponse {
    }
}
