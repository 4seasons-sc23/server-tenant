package com.instream.tenant.domain.host.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class HostHandler {
    public Mono<ServerResponse> hello(ServerRequest request) {
        return ok().body(Mono.just("Hello World!"), String.class);
    }
}
