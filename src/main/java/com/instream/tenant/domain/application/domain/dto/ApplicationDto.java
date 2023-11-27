package com.instream.tenant.domain.application.domain.dto;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationDto(
        UUID id,

        ApplicationType type,

        Status status,

        LocalDateTime createdAt,

        ApplicationSessionDto session

) {
    @Builder
    public ApplicationDto {

    }
}
