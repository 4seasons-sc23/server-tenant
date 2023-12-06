package com.instream.tenant.domain.tenant.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PatchPasswordRequestDto(
    @Schema(example = "testPW1111!", description = "현재 비밀번호")
    String currentPassword,

    @Schema(example = "newPassword!", description = "새로운 비밀번호")
    String newPassword
) {

}
