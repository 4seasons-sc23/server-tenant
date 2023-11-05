package com.instream.tenant.domain.host.domain.dto;

import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.Builder;

import java.util.UUID;

public record TenantDto(
        UUID id,

        String account,

        String name,

        Status status,

        String session
) {
    @Builder
    public TenantDto {

    }
}
