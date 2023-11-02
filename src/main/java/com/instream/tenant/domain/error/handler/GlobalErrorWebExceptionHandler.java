package com.instream.tenant.domain.error.handler;

import com.instream.tenant.domain.error.domain.exception.RestApiException;
import com.instream.tenant.domain.error.domain.response.ErrorResponse;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.EncodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@Order(-2) // webflux default @Order(-1)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    private List<HttpMessageWriter<?>> messageWriters;

    @Autowired
    public GlobalErrorWebExceptionHandler(ServerCodecConfigurer serverCodecConfigurer) {
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof RestApiException restApiException) {
            return writeResponse(restApiException.getHttpErrorCode(), exchange);
        }
        if (ex instanceof RestClientException) {
            return writeResponse(CommonHttpErrorCode.SERVICE_UNAVAILABLE, exchange);
        }

        return writeResponse(CommonHttpErrorCode.INTERNAL_SERVER_ERROR, exchange);
    }

    private Mono<Void> writeResponse(HttpErrorCode httpErrorCode, ServerWebExchange exchange) {
        return Mono.defer(() -> {
            ErrorResponse errorResponse = makeErrorResponse(httpErrorCode);
            ResolvableType elementType = ResolvableType.forInstance(errorResponse);
            @SuppressWarnings("unchecked")
            HttpMessageWriter<ErrorResponse> messageWriter = (HttpMessageWriter<ErrorResponse>) this.messageWriters.stream()
                    .filter(writer -> writer.canWrite(elementType, MediaType.APPLICATION_JSON))
                    .findFirst()
                    .orElseThrow(() -> new EncodingException("No suitable HttpMessageWriter found"));

            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().setStatusCode(httpErrorCode.getHttpStatus());

            return messageWriter.write(Mono.just(errorResponse), elementType, MediaType.APPLICATION_JSON, exchange.getResponse(), Collections.emptyMap());
        });
    }


    private ErrorResponse makeErrorResponse(HttpErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}
