package com.instream.tenant.domain.serviceError.domain.request;

import java.util.UUID;

public record ServiceErrorCreateRequestDto(
    UUID tenantId,
    String title,
    String content
) {}
