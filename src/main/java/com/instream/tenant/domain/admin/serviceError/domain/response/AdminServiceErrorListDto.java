package com.instream.tenant.domain.admin.serviceError.domain.response;

import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorQuestionDto;
import lombok.Builder;

public record AdminServiceErrorListDto(
    ServiceErrorWriterDto writer,
    ServiceErrorQuestionDto question
) {
    @Builder
    public AdminServiceErrorListDto{}
}
