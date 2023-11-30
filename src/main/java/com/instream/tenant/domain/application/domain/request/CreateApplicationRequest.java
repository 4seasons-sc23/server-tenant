package com.instream.tenant.domain.application.domain.request;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;

public record CreateApplicationRequest(
        ApplicationType type
) {
}
