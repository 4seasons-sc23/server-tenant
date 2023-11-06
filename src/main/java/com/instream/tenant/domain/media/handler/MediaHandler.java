package com.instream.tenant.domain.media.handler;

import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.service.ApplicationService;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class MediaHandler {

    private final ApplicationService applicationService;

    @Autowired
    public MediaHandler(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Mono startApplication(ServerRequest request) {
        String authorizationHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID hostId;
        UUID applicationId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return applicationService.startApplication(applicationId, hostId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }

    public Mono endApplication(ServerRequest request) {
        String authorizationHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID hostId;
        UUID applicationId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return applicationService.endApplication(applicationId, hostId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }
}
