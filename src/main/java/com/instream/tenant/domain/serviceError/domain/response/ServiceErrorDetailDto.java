package com.instream.tenant.domain.serviceError.domain.response;

import lombok.Builder;

public record ServiceErrorDetailDto(
    ServiceErrorQuestionDto question,
    ServiceErrorAnswerDto answer
) {
    @Builder
    public ServiceErrorDetailDto {

    }
}
