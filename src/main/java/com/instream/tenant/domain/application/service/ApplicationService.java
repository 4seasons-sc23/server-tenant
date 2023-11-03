package com.instream.tenant.domain.application.service;

import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.response.ApplicationCreateResponse;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class ApplicationService {
    private final ReactiveRedisTemplate<String, ApplicationEntity> redisTemplate;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ReactiveRedisTemplateFactory redisTemplateFactory, ApplicationRepository applicationRepository) {
        this.redisTemplate = redisTemplateFactory.getTemplate(ApplicationEntity.class);
        this.applicationRepository = applicationRepository;
    }

    public Mono<ApplicationCreateResponse> createApplication(ApplicationCreateRequest applicationCreateRequest, UUID hostId) {
        String apiKey = UUID.randomUUID().toString();

        return Mono.defer(() -> applicationRepository.save(
                        ApplicationEntity.builder()
                                .tenantId(hostId)
                                .apiKey(apiKey)
                                .type(applicationCreateRequest.type())
                                .status(Status.USE)
                                .build()
                ))
                .flatMap(application -> redisTemplate.opsForValue()
                        .set(String.valueOf(application.genRedisKey()), application)
                        .thenReturn(application))
                .flatMap(savedApplication -> Mono.just(ApplicationCreateResponse.builder()
                        .apiKey(apiKey)
                        .application(
                                ApplicationDto.builder()
                                        .applicationId(savedApplication.getId())
                                        .type(savedApplication.getType())
                                        .status(savedApplication.getStatus())
                                        .build()
                        )
                        .build()
                ));
    }
}
