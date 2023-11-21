package com.instream.tenant.domain.error.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public record ErrorResponse(
        @JsonProperty
        String code,

        @JsonProperty
        String message
) {
    @Builder
    public ErrorResponse {
    }
}
