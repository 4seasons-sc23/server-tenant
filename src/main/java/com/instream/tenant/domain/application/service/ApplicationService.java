package com.instream.tenant.domain.application.service;

import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.response.ApplicationCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ApplicationService {
    public Mono<ApplicationCreateResponse> createApplication(ApplicationCreateRequest applicationCreateRequest) {
        return Mono.just(ApplicationCreateResponse.builder().build());
    }
}
