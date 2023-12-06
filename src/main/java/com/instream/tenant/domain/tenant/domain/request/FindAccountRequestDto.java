package com.instream.tenant.domain.tenant.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindAccountRequestDto(
    @Schema(example = "010-1234-1234", description = "사용자 전화번호")
    String phoneNumber
) {

}
