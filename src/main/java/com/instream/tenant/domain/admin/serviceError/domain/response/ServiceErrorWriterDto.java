package com.instream.tenant.domain.admin.serviceError.domain.response;

import java.util.UUID;
import lombok.Builder;

public record ServiceErrorWriterDto(
    UUID tenantId,
    String userName,
    String userAccount
) {
    @Builder
    public ServiceErrorWriterDto{}
}
