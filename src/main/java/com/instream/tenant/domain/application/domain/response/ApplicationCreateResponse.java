package com.instream.tenant.domain.application.domain.response;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import lombok.Builder;

public record ApplicationCreateResponse(
        String apiKey,

        ApplicationDto application
) {
    @Builder
    public ApplicationCreateResponse {

    }
}
