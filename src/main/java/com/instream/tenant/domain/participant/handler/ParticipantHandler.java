package com.instream.tenant.domain.participant.handler;

import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ParticipantHandler {
    private final ParticipantService participantService;

    @Autowired
    public ParticipantHandler(ParticipantService participantService) {
        this.participantService = participantService;
    }

    public Mono<ServerResponse> enterToApplication(ServerRequest request) {
        String apiKey = null;
        UUID hostId;
        String participantId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            participantId = request.pathVariable("participantId");
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return request.bodyToMono(EnterToApplicationParticipantRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(enterToApplicationParticipantRequest -> participantService.enterToApplication(apiKey, hostId, participantId, enterToApplicationParticipantRequest))
                .flatMap(participantJoinDto -> ServerResponse.ok().bodyValue(participantJoinDto));
    }

    public Mono leaveFromApplication(ServerRequest request) {
        String apiKey = null;
        UUID hostId;
        String participantId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            participantId = request.pathVariable("participantId");
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return request.bodyToMono(LeaveFromApplicationParticipantRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(applicationSessionId -> participantService.leaveFromApplication(apiKey, hostId, participantId, applicationSessionId))
                .flatMap(participantJoinDto -> ServerResponse.ok().bodyValue(participantJoinDto));
    }
}
