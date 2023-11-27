package com.instream.tenant.domain.serviceError.domain.response;

import com.instream.tenant.domain.common.infra.enums.Status;
import java.time.LocalDateTime;
import lombok.Builder;

public record ServiceErrorAnswerDto(
    String content,
    Status status,
    LocalDateTime createdAt) {

    @Builder
    public ServiceErrorAnswerDto{

    }
}
