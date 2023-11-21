package com.instream.tenant.security.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.error.domain.response.ErrorResponse;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public abstract class CustomWebFilter implements WebFilter {
    private ObjectMapper objectMapper;

    public CustomWebFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected Mono<Void> exchangeUnauthroizedErrorResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

        try {
            ErrorResponse errorResponse = makeErrorResponse(CommonHttpErrorCode.UNAUTHORIZED);
            String json = objectMapper.writeValueAsString(errorResponse);
            DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));

            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            return exchange.getResponse().setComplete();
        }
    }

    private ErrorResponse makeErrorResponse(HttpErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}
