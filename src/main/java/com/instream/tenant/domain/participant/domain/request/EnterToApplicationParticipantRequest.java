package com.instream.tenant.domain.participant.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnterToApplicationParticipantRequest (
        @NotNull
        String participantId,

        @NotBlank
        String nickname,

        String profileImgUrl
) {
}
