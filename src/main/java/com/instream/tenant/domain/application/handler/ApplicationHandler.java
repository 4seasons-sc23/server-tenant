package com.instream.tenant.domain.application.handler;

import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.service.ApplicationService;
import com.instream.tenant.domain.common.infra.model.HandlerHelper;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
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
        return HandlerHelper.getUUIDFromPathVariable(request, "applicationId")
                .flatMap(applicationService::startApplication)
                .flatMap(applicationDto -> ServerResponse.ok().bodyValue(applicationDto));
    }

    public Mono<ServerResponse> endApplication(ServerRequest request) {
        return HandlerHelper.getUUIDFromPathVariable(request, "applicationId")
                .flatMap(applicationService::endApplication)
                .flatMap(applicationDto -> ServerResponse.ok().bodyValue(applicationDto));
    }


    public Mono<ServerResponse> deleteApplication(ServerRequest request) {
        return HandlerHelper.getUUIDFromPathVariable(request, "applicationId")
                .flatMap(applicationService::deleteApplication)
                .then(Mono.defer(() -> ServerResponse.ok().build()));
    }

    public Mono<ServerResponse> searchApplicationSession(ServerRequest request) {
        UUID applicationId;

        try {
            applicationId = UUID.fromString(request.pathVariable("applicationId"));
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return ApplicationSessionSearchPaginationOptionRequest.fromQueryParams(request.queryParams())
                .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
                .flatMap((applicationSessionSearchPaginationOptionRequest -> applicationService.searchSessions(applicationSessionSearchPaginationOptionRequest, applicationId)))
                .flatMap(applicationSessionPaginationDto -> ServerResponse.ok()
                        .bodyValue(applicationSessionPaginationDto));
    }

    public Mono<ServerResponse> startApplicationSession(ServerRequest request) {
        return HandlerHelper.getUUIDFromPathVariable(request, "applicationId")
                .flatMap(applicationService::startApplicationSession)
                .flatMap(applicationSessionDto -> ServerResponse.ok().bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> endApplicationSession(ServerRequest request) {
        return HandlerHelper.getUUIDFromPathVariable(request, "applicationId")
                .flatMap(applicationService::endApplicationSession)
                .flatMap(applicationSessionDto -> ServerResponse.ok().bodyValue(applicationSessionDto));
    }
}
