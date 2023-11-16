package com.instream.tenant.domain.participant.handler;

import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.request.SendMessageParticipantRequest;
import com.instream.tenant.domain.participant.service.MessageService;
import com.instream.tenant.domain.participant.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ParticipantHandler {
    private final ParticipantService participantService;

    private final MessageService messageService;

    @Autowired
    public ParticipantHandler(ParticipantService participantService, MessageService messageService) {
        this.participantService = participantService;
        this.messageService = messageService;
    }

    public Mono<ServerResponse> enterToApplication(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

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
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

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

    public Mono<ServerResponse> searchParticipantJoin(ServerRequest request) {
        UUID hostId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return ParticipantJoinSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((participantJoinSearchPaginationOptionRequest -> participantService.searchParticipantJoin(participantJoinSearchPaginationOptionRequest, hostId)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }

    public Mono<ServerResponse> searchParticipantJoinWithApplicationSession(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID hostId;
        UUID applicationSessionId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationSessionId = UUID.fromString(request.pathVariable("sessionId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return ParticipantJoinSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((participantJoinSearchPaginationOptionRequest -> participantService.searchParticipantJoinWithApplication(participantJoinSearchPaginationOptionRequest, hostId, applicationSessionId)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }

    public Mono<ServerResponse> sendMessage(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID hostId;
        String participantId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            participantId = request.pathVariable("participantId");
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return request.bodyToMono(SendMessageParticipantRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((sendMessageParticipantRequest -> messageService.sendMessage(apiKey, hostId, participantId, sendMessageParticipantRequest)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }
}
