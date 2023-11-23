package com.instream.tenant.domain.serviceError.service;

import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.dto.ServiceErrorDto;
import com.instream.tenant.domain.serviceError.infra.enums.ServiceErrorErrorCode;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ServiceErrorService {
    private final ServiceErrorRepository serviceErrorRepository;

    @Autowired
    public ServiceErrorService(ServiceErrorRepository serviceErrorRepository) {
        this.serviceErrorRepository = serviceErrorRepository;
    }

    public Mono<ServiceErrorDto> getServiceErrorById(Long errorId) {
        return serviceErrorRepository.findByErrorId(errorId)
            .map(error -> ServiceErrorDto.builder()
                .errorId(error.getErrorId())
                .tenantId(error.getTenantId())
                .title(error.getTitle())
                .content(error.getContent())
                .isAnswered(error.getIsAnswered())
                .status(error.getStatus())
                .createdAt(error.getCreatedAt())
                .updatedAt(error.getUpdatedAt())
            .build())
            .switchIfEmpty(Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)));
    }
}