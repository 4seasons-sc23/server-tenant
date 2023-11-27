package com.instream.tenant.domain.participant.domain.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LeaveFromApplicationParticipantRequest(
        @NotNull
        String participantId
) {
}
