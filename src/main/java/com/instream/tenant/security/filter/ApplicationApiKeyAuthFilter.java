package com.instream.tenant.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationApiKeyAuthFilter extends CustomWebFilter {
    private final List<String> API_WHITE_LIST = new ArrayList<>(Arrays.asList("/api"));
    private final ApplicationRepository applicationRepository;

    public ApplicationApiKeyAuthFilter(ApplicationRepository applicationRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (API_WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst("ApiKey");

        if (apiKey == null || apiKey.isEmpty()) {
            return super.exchangeUnauthroizedErrorResponse(exchange);
        }

        return applicationRepository.existsByApiKey(apiKey)
                .flatMap(exists -> {
                    if (exists) {
                        return chain.filter(exchange);
                    } else {
                        return super.exchangeUnauthroizedErrorResponse(exchange);
                    }
                });
    }
}
