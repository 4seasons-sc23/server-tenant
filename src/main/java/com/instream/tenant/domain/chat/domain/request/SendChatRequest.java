package com.instream.tenant.domain.chat.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SendChatRequest(
        @NotBlank
        @Schema(description = "참가자 ID", example = "123123abc")
        String participantId,

        @NotBlank
        @Schema(description = "채팅 메세지", example = "123123sdafasdf")
        String message
) {
}
