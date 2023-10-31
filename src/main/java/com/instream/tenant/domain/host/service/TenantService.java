package com.instream.tenant.domain.host.service;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.host.domain.dto.TenantDto;
import com.instream.tenant.domain.host.domain.entity.TenantEntity;
import com.instream.tenant.domain.host.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.host.repository.TenantRepository;
import com.instream.tenant.domain.redis.factory.ReactiveRedisTemplateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class TenantService {
    private ReactiveRedisTemplate<String, TenantEntity> redisTemplate;

    private TenantRepository tenantRepository;

    @Autowired
    public TenantService(ReactiveRedisTemplateFactory redisTemplateFactory, TenantRepository tenantRepository) {
        this.redisTemplate = redisTemplateFactory.getTemplate(TenantEntity.class);
        this.tenantRepository = tenantRepository;
    }

    public Mono<TenantDto> getTenantByAccount(String account) {
        return redisTemplate.opsForValue()
                .get(account)
                .switchIfEmpty(Mono.defer(() -> tenantRepository.findByAccount(account)
                        .flatMap(tenant -> redisTemplate.opsForValue()
                                .set(account, tenant)
                                .thenReturn(tenant))))
                // TODO: Tenant mapper
                .map(tenant -> TenantDto.builder()
                        .name(tenant.getName())
                        .status(tenant.getStatus())
                        // TODO: 세션
                        .session("")
                        .build());
    }


    public Mono<TenantDto> createTenant(TenantCreateRequest tenantCreateRequest) {
        return tenantRepository.findByAccount(tenantCreateRequest.account())
                // TODO: 글로벌 에러 핸들링
                .flatMap(existingTenant -> Mono.<TenantDto>error(new RuntimeException("Tenant already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    TenantEntity newTenant = TenantEntity.builder()
                            .account(tenantCreateRequest.account())
                            .password(tenantCreateRequest.password())
                            .name(tenantCreateRequest.name())
                            .phoneNum(tenantCreateRequest.phoneNumber())
                            .status(Status.USE)
                            .secretKey("")
                            .build();

                    return tenantRepository.save(newTenant)
                            .flatMap(savedTenant -> Mono.just(TenantDto.builder()
                                    .id(savedTenant.getId())
                                    .account(savedTenant.getAccount())
                                    .name(savedTenant.getName())
                                    .status(savedTenant.getStatus())
                                    .session("")
                                    .build()
                            ));
                }));
        // TODO:
        //  1. 캐싱 필요한지?
        //  2. 필요하다면 DTO랑 Entity 중에 어떤 걸 캐싱할 건지?
        //  3. DTO랑 Entity 캐싱할 때 key 값 분리는 어떻게 할건지?
    }
}
