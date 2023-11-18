package com.instream.tenant.domain.media.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.instream.tenant.domain.media.handler.MediaHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MediaRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> v1MediaRoutes(MediaHandler mediaHandler) {
        return RouterFunctions.route(RequestPredicates.POST("/v1/hls/upload/{quality}"), mediaHandler::uploadMedia);
    }
}
