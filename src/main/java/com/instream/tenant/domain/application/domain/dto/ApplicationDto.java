package com.instream.tenant.domain.application.domain.dto;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.Builder;

import java.util.UUID;

public record ApplicationDto(
        UUID applicationId,

        ApplicationType type,

        String session,

        Status status
) {
    @Builder
    public ApplicationDto {

    }
}
