package com.instream.tenant.domain.host.config;

import com.instream.tenant.domain.host.router.HostRouterHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Supplier;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class HostRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1Routes(HostRouterHandler hostRouterHandler) {
        return route().nest(RequestPredicates.path("/v1"),
                helloFunctionSupplier(hostRouterHandler),
                ops -> ops.operationId("123")
        ).build();
    }

    @Bean
    public RouterFunction<ServerResponse> v2Routes(HostRouterHandler hostRouterHandler) {
        return route().nest(RequestPredicates.path("/v2"),
                helloFunctionSupplier(hostRouterHandler),
                ops -> ops.operationId("123")
        ).build();
    }

    private Supplier<RouterFunction<ServerResponse>> helloFunctionSupplier(HostRouterHandler hostRouterHandler) {
        return () -> route()
                .GET("/hello", hostRouterHandler::hello, ops -> ops.operationId("123"))
                .build();
    }
}