package com.instream.tenant.domain.serviceError.handler;

import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.dto.ServiceErrorDto;
import com.instream.tenant.domain.serviceError.service.ServiceErrorService;
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

    public Mono<ServerResponse> getService(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));

        return serviceErrorService.getServiceErrorById(errorId)
            .flatMap(error -> ServerResponse.ok().bodyValue(error))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
}
