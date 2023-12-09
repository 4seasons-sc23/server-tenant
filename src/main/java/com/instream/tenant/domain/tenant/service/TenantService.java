package com.instream.tenant.domain.tenant.service;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.tenant.domain.dto.TenantDto;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import com.instream.tenant.domain.tenant.domain.request.FindAccountRequestDto;
import com.instream.tenant.domain.tenant.domain.request.FindPasswordRequestDto;
import com.instream.tenant.domain.tenant.domain.request.PatchPasswordRequestDto;
import com.instream.tenant.domain.tenant.domain.request.PatchTenantNameRequestDto;
import com.instream.tenant.domain.tenant.domain.request.TenantCreateRequest;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import com.instream.tenant.domain.tenant.domain.response.FindAccountResponseDto;
import com.instream.tenant.domain.tenant.domain.response.HostWithdrawalResponseDto;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import com.instream.tenant.domain.tenant.repository.TenantRepository;
import com.instream.tenant.domain.redis.model.factory.ReactiveRedisTemplateFactory;

import java.time.LocalDateTime;
import java.util.Objects;

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
                        .status(tenant.getStatus())
                        // TODO: 세션
                        .build());
    }

    public Mono<TenantDto> signIn(TenantSignInRequest tenantSignInRequest) {
        return tenantRepository.findByAccount(tenantSignInRequest.account())
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(tenant -> {
                    if (passwordEncoder.matches(tenantSignInRequest.password(), tenant.getPassword())) {
                        return Mono.just(TenantDto.builder()
                                .id(tenant.getId())
                                .account(tenant.getAccount())
                                .name(tenant.getName())
                                .phoneNumber(tenant.getPhoneNumber())
                                .status(tenant.getStatus())
                                .build());
                    }

                    return Mono.error(new RestApiException(TenantErrorCode.UNAUTHORIZED));
                });
//        return tenantRepository.findByAccountAndPassword(tenantSignInRequest.account(), passwordEncoder.encode(tenantSignInRequest.password()))
//                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
//                .flatMap(tenant -> Mono.just(TenantDto.builder()
//                        .id(tenant.getId())
//                        .account(tenant.getAccount())
//                        .name(tenant.getName())
//                        .phoneNumber(tenant.getPhoneNumber())
//                        .status(tenant.getStatus())
//                        .build()));
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
                                        .status(savedTenant.getStatus())
                                        .build()
                                ))
                );
    }

    public Mono<FindAccountResponseDto> findAccountByPhoneNum(FindAccountRequestDto requestDto) {
        return tenantRepository.findByPhoneNumberAndStatus(requestDto.phoneNumber(), Status.USE)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(accountDto -> Mono.just(FindAccountResponseDto.builder()
                        .account(accountDto.getAccount())
                        .createdAt(accountDto.getCreatedAt())
                        .build()));
    }

    public Mono<Void> findPasswordByPhoneNum(FindPasswordRequestDto requestDto) {
        return tenantRepository.findByPhoneNumberAndStatus(requestDto.userPhoneNum(), Status.USE)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(tenantEntity -> {
                    tenantEntity.setPassword(requestDto.newPassword());
                    return tenantRepository.save(tenantEntity);
                })
                .then();
    }

    public Mono<Void> checkDuplicateAccount(String account) {
        return tenantRepository.findByAccount(account)
                .flatMap(tenantEntity -> {
                    if (tenantEntity != null) {
                        return Mono.error(new RestApiException(TenantErrorCode.EXIST_ACCOUNT));
                    }
                    return Mono.empty();
                })
                .then();
    }

    public Mono<HostWithdrawalResponseDto> withdrawal(UUID hostId) {
        return tenantRepository.findByIdAndStatus(hostId, Status.USE)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(tenantEntity -> {
                    tenantEntity.setStatus(Status.DELETED);
                    tenantEntity.setDeletedAt(LocalDateTime.now());
                    return tenantRepository.save(tenantEntity)
                            .flatMap(savedTenant -> Mono.just(HostWithdrawalResponseDto.builder()
                                    .hostId(savedTenant.getId())
                                    .status(savedTenant.getStatus())
                                    .deletedAt(savedTenant.getDeletedAt())
                                    .build()));
                });
    }

    public Mono<Void> patchPassword(UUID hostId, PatchPasswordRequestDto requestDto) {
        return tenantRepository.findByIdAndStatus(hostId, Status.USE)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(tenantEntity -> {
                    if (!Objects.equals(tenantEntity.getPassword(), requestDto.currentPassword())) {
                        return Mono.error(new RestApiException(TenantErrorCode.UNAUTHORIZED));
                    }
                    tenantEntity.setPassword(requestDto.newPassword());
                    return tenantRepository.save(tenantEntity);
                })
                .then();
    }

    public Mono<TenantDto> patchHostName(UUID hostId, PatchTenantNameRequestDto requestDto) {
        return tenantRepository.findByIdAndStatus(hostId, Status.USE)
                .switchIfEmpty(Mono.error(new RestApiException(TenantErrorCode.TENANT_NOT_FOUND)))
                .flatMap(tenantEntity -> {
                    tenantEntity.setName(requestDto.name());
                    return tenantRepository.save(tenantEntity);
                })
                .flatMap(savedTenant -> Mono.just(TenantDto.builder()
                        .id(savedTenant.getId())
                        .account(savedTenant.getAccount())
                        .name(savedTenant.getName())
                        .phoneNumber(savedTenant.getPhoneNumber())
                        .status(savedTenant.getStatus())
                        .build()));
    }
}
