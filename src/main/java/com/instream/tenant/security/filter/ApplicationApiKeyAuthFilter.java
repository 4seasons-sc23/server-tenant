package com.instream.tenant.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ApplicationApiKeyAuthFilter extends CustomWebFilter {
    private final List<Pattern> API_BLACK_LIST = new ArrayList<>(Arrays.asList(
            Pattern.compile("/api/v1/chats/.*"),
            Pattern.compile("/api/v1/medias/upload.*"),
            Pattern.compile("/api/v1/applications/.*")
    ));
    private final ApplicationRepository applicationRepository;

    public ApplicationApiKeyAuthFilter(ApplicationRepository applicationRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (API_BLACK_LIST.stream().noneMatch(pattern -> pattern.matcher(path).find())) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(InstreamHttpHeaders.API_KEY);

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
