package com.instream.tenant.domain.serviceError.domain.dto;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

public record ServiceErrorDto(
    Long errorId,
    UUID tenantId,
    String title,
    String content,
    IsAnswered isAnswered,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

    @Builder
    public ServiceErrorDto{

    }
}
