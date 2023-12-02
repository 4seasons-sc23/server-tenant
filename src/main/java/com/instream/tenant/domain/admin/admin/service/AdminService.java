package com.instream.tenant.domain.admin.admin.service;

import com.instream.tenant.domain.admin.admin.domain.dto.AdminDto;
import com.instream.tenant.domain.admin.admin.domain.request.AdminSignInRequest;
import com.instream.tenant.domain.admin.admin.infra.enums.AdminErrorCode;
import com.instream.tenant.domain.admin.admin.repository.AdminRepository;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.tenant.domain.request.TenantSignInRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Mono<AdminDto> getAdminById(long adminId) {
        return adminRepository.findById(adminId)
                .switchIfEmpty(Mono.error(new RestApiException(AdminErrorCode.ADMIN_NOT_FOUND)))
                .flatMap(admin -> Mono.just(AdminDto.builder()
                        .id(adminId)
                        .name(admin.getName())
                        .account(admin.getAccount())
                        .status(admin.getStatus())
                        .build()));
    }

    public Mono<AdminDto> signIn(AdminSignInRequest adminSignInRequest) {
        return adminRepository.findByAccountAndPassword(adminSignInRequest.account(), adminSignInRequest.password())
                .switchIfEmpty(Mono.error(new RestApiException(AdminErrorCode.ADMIN_NOT_FOUND)))
                .map(admin -> AdminDto.builder()
                        .id(admin.getId())
                        .account(admin.getAccount())
                        .name(admin.getName())
                        .status(admin.getStatus())
                        .build());
    }

}
