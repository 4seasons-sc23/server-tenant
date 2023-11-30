package com.instream.tenant.domain.chat.handler;

import com.instream.tenant.domain.chat.domain.request.SendChatRequest;
import com.instream.tenant.domain.common.infra.helper.HandlerHelper;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.participant.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class ChatHandler {
    private final ParticipantService participantService;

    @Autowired
    public ChatHandler(ParticipantService participantService) {
        this.participantService = participantService;
    }

    public Mono<ServerResponse> sendChat(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);
        Mono<UUID> applicationSessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");

        // TODO: validation send chat request empty string property
        return Mono.zip(applicationSessionIdMono, request.bodyToMono(SendChatRequest.class))
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap(tuple -> participantService.sendChat(apiKey, tuple.getT1(), tuple.getT2()))
                .then(ServerResponse.created(URI.create("")).build());
    }
}
