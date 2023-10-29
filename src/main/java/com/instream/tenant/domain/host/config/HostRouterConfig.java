package com.instream.tenant.domain.host.config;

import com.instream.tenant.domain.host.router.HostRouterHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class HostRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(HostRouterHandler hostRouterHandler) {
        return route().GET("/hello", hostRouterHandler::hello, ops -> ops
                .operationId("hello")
        ).build();
    }
}