package com.instream.tenant.domain.serviceError.domain.request;

import java.util.UUID;

public record ServiceErrorCreateRequest (
    UUID hostId,
    String title,
    String content
) {}
