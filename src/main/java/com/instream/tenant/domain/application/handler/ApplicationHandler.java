package com.instream.tenant.domain.application.handler;

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
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
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
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return request.bodyToMono(ApplicationCreateRequest.class)
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((applicationCreateRequest -> applicationService.createApplication(applicationCreateRequest, hostId)))
                .flatMap(applicationDto -> ServerResponse.created(URI.create(String.format("/%s/info", applicationDto.applicationId())))
                        .bodyValue(applicationDto));
    }

    public Mono<ServerResponse> startApplication(ServerRequest request) {
        String apiKey = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID applicationId;

        try {
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return applicationService.startApplication(apiKey, applicationId).flatMap((applicationSessionId -> ServerResponse.ok().bodyValue(applicationSessionId)));
    }

    public Mono<ServerResponse> endApplication(ServerRequest request) {
        String apiKey = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID applicationId;

        try {
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return applicationService.endApplication(apiKey, applicationId).flatMap((applicationSessionId -> ServerResponse.ok().bodyValue(applicationSessionId)));
    }


    public Mono deleteApplication(ServerRequest request) {
        String apiKey = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
        }

        UUID applicationId;

        try {
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return applicationService.deleteApplication(apiKey, applicationId).then(Mono.defer(() -> ServerResponse.ok().build()));
    }
}
