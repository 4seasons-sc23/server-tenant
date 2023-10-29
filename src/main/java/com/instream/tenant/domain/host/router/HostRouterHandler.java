package com.instream.tenant.domain.host.router;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class HostRouterHandler {
    public Mono<ServerResponse> hello(ServerRequest request) {
        return ok().body(Mono.just("Hello World!"), String.class);
    }
}
