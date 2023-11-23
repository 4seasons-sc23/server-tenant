package com.instream.tenant.domain.tenant.config;

import com.instream.tenant.domain.serviceError.handler.ServiceErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HostServiceErrorConfig {
    @Bean
    public RouterFunction<ServerResponse> v1ServiceErrorFunction(ServiceErrorHandler serviceErrorHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/v1/errors/{errorId}"), serviceErrorHandler::getService);
//            .andRoute(RequestPredicates.GET("/services"), handler::getServices)
//            .andRoute(RequestPredicates.POST("/service"), handler::createService)
//            .andRoute(RequestPredicates.PUT("/service/{id}"), handler::updateService)
//            .andRoute(RequestPredicates.DELETE("/service/{id}"), handler::deleteService);
    }
}
