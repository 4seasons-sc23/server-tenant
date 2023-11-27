package com.instream.tenant.domain.serviceError.domain.response;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

public record ServiceErrorQuestionDto (
    Long errorId,
    UUID tenantId,
    String title,
    String content,
    IsAnswered isAnswered,
    Status status,
    LocalDateTime createdAt
) {
    @Builder
    public ServiceErrorQuestionDto {}
}
