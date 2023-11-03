package com.instream.tenant.domain.application.handler;

import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.service.ApplicationService;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class ApplicationHandler {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationHandler(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Mono<ServerResponse> searchApplication(ServerRequest request) {
        UUID hostId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return ApplicationSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((applicationSearchPaginationOptionRequest -> applicationService.search(applicationSearchPaginationOptionRequest, hostId)))
                .flatMap(applicationPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationPaginationDto));
    }

    public Mono<ServerResponse> createApplication(ServerRequest request) {
        UUID hostId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return request.bodyToMono(ApplicationCreateRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((applicationCreateRequest -> applicationService.createApplication(applicationCreateRequest, hostId)))
                .flatMap(applicationCreateResponse -> ServerResponse.created(URI.create(String.format("/%s/info", applicationCreateResponse.application().applicationId())))
                        .bodyValue(applicationCreateResponse));
    }

    public Mono startApplication(ServerRequest request) {
        UUID hostId;
        UUID applicationId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return applicationService.startApplication(applicationId, hostId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }

    public Mono endApplication(ServerRequest request) {
        UUID hostId;
        UUID applicationId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return applicationService.endApplication(applicationId, hostId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }


    public Mono deleteApplication(ServerRequest request) {
        UUID hostId;
        UUID applicationId;

        try {
            hostId = UUID.fromString(request.pathVariable("hostId"));
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RestApiException(CommonHttpErrorCode.BAD_REQUEST);
        }

        return applicationService.deleteApplication(applicationId, hostId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }
}
