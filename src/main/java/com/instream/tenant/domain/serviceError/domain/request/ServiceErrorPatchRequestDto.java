package com.instream.tenant.domain.serviceError.domain.request;

import java.util.UUID;

public record ServiceErrorPatchRequestDto(
    String title,
    String content) {
}
