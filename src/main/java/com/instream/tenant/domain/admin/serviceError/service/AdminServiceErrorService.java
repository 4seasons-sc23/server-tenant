package com.instream.tenant.domain.admin.serviceError.service;

import com.instream.tenant.domain.admin.serviceError.domain.request.ServiceErrorAnswerRequestDto;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorAnswerEntity;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorEntity;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorAnswerDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorCreateResponseDto;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorAnswerRepository;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AdminServiceErrorService {
    private final ServiceErrorRepository serviceErrorRepository;
    private final ServiceErrorAnswerRepository serviceErrorAnswerRepository;

    public AdminServiceErrorService(ServiceErrorRepository serviceErrorRepository,
        ServiceErrorAnswerRepository serviceErrorAnswerRepository) {
        this.serviceErrorRepository = serviceErrorRepository;
        this.serviceErrorAnswerRepository = serviceErrorAnswerRepository;
    }

    public Mono<ServiceErrorAnswerDto> postServiceErrorAnswer(
        ServiceErrorAnswerRequestDto request, Long errorId) {
        return serviceErrorAnswerRepository.save(
                ServiceErrorAnswerEntity.builder()
                    .errorId(errorId)
                    .content(request.answerContent())
                    .status(Status.USE)
                    .build())
            .flatMap(savedServiceError -> Mono.just(ServiceErrorAnswerDto.builder()
                .content(savedServiceError.getContent())
                .status(savedServiceError.getStatus())
                .createdAt(savedServiceError.getCreatedAt())
                .build())
            );
    }
}
