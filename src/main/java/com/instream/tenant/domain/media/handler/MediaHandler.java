package com.instream.tenant.domain.media.handler;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.media.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class MediaHandler {

    private final MediaService mediaService;

    @Autowired
    public MediaHandler(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public Mono<ServerResponse> startLive(ServerRequest request) {
        String apiKey = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        return mediaService.startLive(apiKey).flatMap(applicationSession -> ServerResponse.ok().bodyValue(applicationSession));
    }

    public Mono<ServerResponse> endLive(ServerRequest request) {
        String apiKey = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        return mediaService.endLive(apiKey).flatMap(applicationSession -> ServerResponse.ok().bodyValue(applicationSession));
    }
}
