package com.instream.tenant.domain.tenant.service;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import com.instream.tenant.domain.tenant.repository.TenantRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class TenantService {
    private ReactiveRedisTemplate<String, TenantEntity> redisTemplate;

    private PasswordEncoder passwordEncoder;

    private TenantRepository tenantRepository;


    @Autowired
    public TenantService(ReactiveRedisTemplateFactory redisTemplateFactory, TenantRepository tenantRepository, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplateFactory.getTemplate(TenantEntity.class);
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<TenantDto> getTenantById(UUID tenantId) {
        return redisTemplate.opsForValue()
                .get(tenantId.toString())
                .switchIfEmpty(Mono.defer(() -> tenantRepository.findById(tenantId)
                                .flatMap(tenant -> redisTemplate.opsForValue()
                                        .set(String.valueOf(tenant.genRedisKey()), tenant)
                                        .thenReturn(tenant)))
                        .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                )
                // TODO: Tenant mapper
                .map(tenant -> TenantDto.builder()
                        .id(tenant.getId())
                        .account(tenant.getAccount())
                        .name(tenant.getName())
                        .phoneNumber(tenant.getPhoneNumber())
                        .apiKey(tenant.getApiKey())
                        .status(tenant.getStatus())
                        // TODO: 세션
                        .build());
    }

    public Mono<TenantDto> signIn(TenantSignInRequest tenantSignInRequest) {
        return tenantRepository.findByAccountAndPassword(tenantSignInRequest.account(), passwordEncoder.encode(tenantSignInRequest.password()))
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .map(tenant -> TenantDto.builder()
                        .id(tenant.getId())
                        .account(tenant.getAccount())
                        .name(tenant.getName())
                        .phoneNumber(tenant.getPhoneNumber())
                        .apiKey(tenant.getApiKey())
                        .status(tenant.getStatus())
                        .build());
    }

    public Mono<TenantDto> signUp(TenantCreateRequest tenantCreateRequest) {
        return tenantRepository.findByAccount(tenantCreateRequest.account())
                .flatMap(existingTenant -> Mono.<TenantDto>error(new RestApiException(TenantErrorCode.EXIST_TENANT)))
                .switchIfEmpty(Mono.defer(() -> tenantRepository.save(
                                        TenantEntity.builder()
                                                .account(tenantCreateRequest.account())
                                                .password(passwordEncoder.encode(tenantCreateRequest.password()))
                                                .name(tenantCreateRequest.name())
                                                .phoneNumber(tenantCreateRequest.phoneNumber())
                                                .apiKey(UUID.randomUUID().toString())
                                                .status(Status.USE)
                                                .build()
                                ))
                                .flatMap(tenant -> redisTemplate.opsForValue()
                                        .set(String.valueOf(tenant.genRedisKey()), tenant)
                                        .thenReturn(tenant))
                                .flatMap(savedTenant -> Mono.just(TenantDto.builder()
                                        .id(savedTenant.getId())
                                        .account(savedTenant.getAccount())
                                        .name(savedTenant.getName())
                                        .phoneNumber(savedTenant.getPhoneNumber())
                                        .apiKey(savedTenant.getApiKey())
                                        .status(savedTenant.getStatus())
                                        .build()
                                ))
                );
    }
}
