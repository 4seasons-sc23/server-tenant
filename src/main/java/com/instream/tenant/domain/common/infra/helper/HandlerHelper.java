package com.instream.tenant.domain.common.infra.helper;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class HandlerHelper {
    static public Mono<UUID> getUUIDFromPathVariable(ServerRequest request, String variableName) {
        try {
            UUID result = UUID.fromString(request.pathVariable(variableName));
            return Mono.just(result);
        } catch (IllegalArgumentException illegalArgumentException) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
