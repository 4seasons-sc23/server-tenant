package com.instream.tenant.domain.participant.handler;

import com.instream.tenant.domain.common.infra.model.HandlerHelper;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.domain.request.SendMessageParticipantRequest;
import com.instream.tenant.domain.participant.service.ParticipantService;
import org.jetbrains.annotations.NotNull;
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
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);
        Mono<UUID> sessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");
        Mono<EnterToApplicationParticipantRequest> enterRequesetMono = request.bodyToMono(EnterToApplicationParticipantRequest.class);
        return Mono.zip(sessionIdMono, enterRequesetMono)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tuple -> participantService.enterToApplication(apiKey, tuple.getT1(), tuple.getT2()))
                .flatMap(participantJoinDto -> ServerResponse.ok().bodyValue(participantJoinDto));
    }

    public Mono leaveFromApplication(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);
        Mono<UUID> sessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");
        Mono<LeaveFromApplicationParticipantRequest> leaveRequesetMono = request.bodyToMono(LeaveFromApplicationParticipantRequest.class);

        return Mono.zip(sessionIdMono, leaveRequesetMono)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tuple -> participantService.leaveFromApplication(apiKey, tuple.getT1(), tuple.getT2()))
                .flatMap(participantJoinDto -> ServerResponse.ok().bodyValue(participantJoinDto));
    }

    public Mono<ServerResponse> searchParticipantJoinWithTenant(ServerRequest request) {
        Mono<UUID> hostIdMono = HandlerHelper.getUUIDFromPathVariable(request, "hostId");
        Mono<ParticipantJoinSearchPaginationOptionRequest> participantJoinSearchPaginationOptionRequestMono = ParticipantJoinSearchPaginationOptionRequest.fromQueryParams(request.queryParams());
        return searchParticipantJoin(null, hostIdMono, participantJoinSearchPaginationOptionRequestMono);
    }

    public Mono<ServerResponse> searchParticipantJoinWithSession(ServerRequest request) {
        Mono<UUID> sessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");
        Mono<ParticipantJoinSearchPaginationOptionRequest> participantJoinSearchPaginationOptionRequestMono = ParticipantJoinSearchPaginationOptionRequest.fromQueryParams(request.queryParams());
        return searchParticipantJoin(null, sessionIdMono, participantJoinSearchPaginationOptionRequestMono);
    }

    @NotNull
    private Mono<ServerResponse> searchParticipantJoin(Mono<UUID> hostIdMono, Mono<UUID> sessionIdMono, Mono<ParticipantJoinSearchPaginationOptionRequest> participantJoinSearchPaginationOptionRequestMono) {
        return Mono.zip(participantJoinSearchPaginationOptionRequestMono, hostIdMono, sessionIdMono)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((tuple -> participantService.searchParticipantJoin(tuple.getT1(), tuple.getT2(), tuple.getT3())))
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
                .flatMap((sendMessageParticipantRequest -> participantService.sendMessage(apiKey, hostId, participantId, sendMessageParticipantRequest)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }
}
