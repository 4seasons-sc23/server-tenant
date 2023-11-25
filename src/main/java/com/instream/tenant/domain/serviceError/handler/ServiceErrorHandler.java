package com.instream.tenant.domain.serviceError.handler;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorPatchRequestDto;
import com.instream.tenant.domain.serviceError.service.ServiceErrorService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ServiceErrorHandler {

    private final ServiceErrorService serviceErrorService;

    @Autowired
    public ServiceErrorHandler(ServiceErrorService serviceErrorService) {
        this.serviceErrorService = serviceErrorService;
    }

    public Mono<ServerResponse> getServiceError(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));

        return serviceErrorService.getServiceErrorById(errorId)
            .flatMap(error -> ServerResponse.ok().bodyValue(error))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> postServiceError(ServerRequest request) {
        return request.bodyToMono(ServiceErrorCreateRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(serviceErrorService::postServiceError)
            .flatMap(serviceErrorDto -> ServerResponse.created(URI.create(String.format("/errors/%s", serviceErrorDto.errorId())))
            .bodyValue(serviceErrorDto));
    }

    public Mono<ServerResponse> patchServiceError(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));

        return request.bodyToMono(ServiceErrorPatchRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(patchRequestDto -> serviceErrorService.patchServiceError(errorId, patchRequestDto))
            .flatMap(serviceError -> ServerResponse.ok().bodyValue(serviceError));
    }
}
