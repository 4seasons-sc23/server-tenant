package com.instream.tenant.domain.serviceError.domain.request;

import java.util.UUID;

public record ServiceErrorCreateRequest (
    UUID tenantId,
    String title,
    String content
) {}
