package com.instream.tenant.domain.participant.handler;

import com.instream.tenant.domain.common.infra.helper.HandlerHelper;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.LeaveFromApplicationParticipantRequest;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
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

    public Mono<ServerResponse> leaveFromApplication(ServerRequest request) {
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
        return Mono.zip(participantJoinSearchPaginationOptionRequestMono, hostIdMono)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((tuple -> participantService.searchParticipantJoin(tuple.getT1(), tuple.getT2(), null)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }

    public Mono<ServerResponse> searchParticipantJoinWithSession(ServerRequest request) {
        Mono<UUID> sessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");
        Mono<ParticipantJoinSearchPaginationOptionRequest> participantJoinSearchPaginationOptionRequestMono = ParticipantJoinSearchPaginationOptionRequest.fromQueryParams(request.queryParams());
        return Mono.zip(participantJoinSearchPaginationOptionRequestMono, sessionIdMono)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((tuple -> participantService.searchParticipantJoin(tuple.getT1(), null, tuple.getT2())))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }
}
