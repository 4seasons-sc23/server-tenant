package com.instream.tenant.domain.admin.admin.domain.dto;

import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.Builder;

import java.util.UUID;

public record AdminDto(
        Long id,

        String account,

        String name,

        Status status,

        String session
) {
    @Builder
    public AdminDto {

    }
}

