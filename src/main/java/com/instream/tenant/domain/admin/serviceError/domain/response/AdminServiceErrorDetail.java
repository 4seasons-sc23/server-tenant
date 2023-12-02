package com.instream.tenant.domain.admin.serviceError.domain.response;

import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorAnswerDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorQuestionDto;
import lombok.Builder;

public record AdminServiceErrorDetail(
    ServiceErrorWriterDto writer,
    ServiceErrorQuestionDto question,
    ServiceErrorAnswerDto answer

) {
    @Builder
    public AdminServiceErrorDetail{}
}
