package com.instream.tenant.domain.application.service;

import com.instream.tenant.domain.application.domain.request.ApplicationSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.model.specification.ApplicationSpecification;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.request.ApplicationCreateRequest;
import com.instream.tenant.domain.application.domain.response.ApplicationCreateResponse;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
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

    public Mono<PaginationDto<CollectionDto<ApplicationDto>>> search(ApplicationSearchPaginationOptionRequest applicationSearchPaginationOptionRequest, UUID hostId) {
        Pageable pageable = applicationSearchPaginationOptionRequest.getPageable();
        Predicate predicate = ApplicationSpecification.with(applicationSearchPaginationOptionRequest);

        Flux<ApplicationEntity> applicationFlux = applicationRepository.findBy(predicate, pageable);
        Mono<List<ApplicationDto>> applicationDtoListMono = applicationFlux
                .map(applicationEntity -> ApplicationDto.builder()
                        .applicationId(applicationEntity.getId())
                        .session("")
                        .type(applicationEntity.getType())
                        .status(applicationEntity.getStatus())
                        .createdAt(applicationEntity.getCreatedAt())
                        .build()
                )
                .collectList();

        // TODO: Redis 캐싱 넣기
        if (applicationSearchPaginationOptionRequest.isFirstView()) {
            Mono<Long> totalElementCountMono = applicationRepository.count(predicate);
            Mono<Integer> totalPageCountMono = totalElementCountMono.map(count -> (int) Math.ceil((double) count / pageable.getPageSize()));

            return Mono.zip(applicationDtoListMono, totalPageCountMono, totalElementCountMono)
                    .map(tuple -> {
                        List<ApplicationDto> applications = tuple.getT1();
                        Integer pageCount = tuple.getT2();
                        int totalElementCount = tuple.getT3().intValue();

                        return PaginationInfoDto.<CollectionDto<ApplicationDto>>builder()
                                .totalElementCount(totalElementCount)
                                .pageCount(pageCount)
                                .currentPage(applicationSearchPaginationOptionRequest.getPage())
                                .data(CollectionDto.<ApplicationDto>builder()
                                        .data(applications)
                                        .build())
                                .build();
                    });
        }

        return applicationDtoListMono.map(applicationDtoList -> PaginationDto.<CollectionDto<ApplicationDto>>builder()
                .currentPage(applicationSearchPaginationOptionRequest.getPage())
                .data(CollectionDto.<ApplicationDto>builder()
                        .data(applicationDtoList)
                        .build())
                .build()
        );
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
