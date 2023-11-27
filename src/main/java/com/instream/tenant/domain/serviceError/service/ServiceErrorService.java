package com.instream.tenant.domain.serviceError.service;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.dto.ServiceErrorDto;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorAnswerEntity;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorEntity;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorPatchRequestDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorAnswerDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorCreateResponseDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorDetailDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorQuestionDto;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import com.instream.tenant.domain.serviceError.infra.enums.ServiceErrorErrorCode;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorAnswerRepository;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ServiceErrorService {

    private final ServiceErrorRepository serviceErrorRepository;
    private final ServiceErrorAnswerRepository serviceErrorAnswerRepository;

    @Autowired
    public ServiceErrorService(ServiceErrorRepository serviceErrorRepository,
        ServiceErrorAnswerRepository serviceErrorAnswerRepository) {
        this.serviceErrorRepository = serviceErrorRepository;
        this.serviceErrorAnswerRepository = serviceErrorAnswerRepository;
    }

    public Mono<ServiceErrorDetailDto> getServiceErrorById(Long errorId) {
        Mono<ServiceErrorEntity> errorEntityMono = serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)));

        Mono<ServiceErrorAnswerEntity> answerEntityMono = serviceErrorAnswerRepository.findByErrorId(errorId)
            .switchIfEmpty(Mono.just(ServiceErrorAnswerEntity.builder().build()));

        return Mono.zip(errorEntityMono, answerEntityMono)
            .map(tuple -> {
                ServiceErrorEntity question = tuple.getT1();
                ServiceErrorAnswerEntity answer = tuple.getT2();

                ServiceErrorQuestionDto questionDto = ServiceErrorQuestionDto.builder()
                    .errorId(question.getErrorId())
                    .title(question.getTitle())
                    .tenantId(question.getTenantId())
                    .content(question.getContent())
                    .isAnswered(question.getIsAnswered())
                    .status(question.getStatus())
                    .createdAt(question.getCreatedAt())
                    .build();

                ServiceErrorAnswerDto answerDto = IsAnswered.ANSWERED.equals(question.getIsAnswered()) ? null :
                    ServiceErrorAnswerDto.builder()
                        .content(answer.getContent())
                        .status(answer.getStatus())
                        .createdAt(answer.getCreatedAt())
                        .build();

                return ServiceErrorDetailDto.builder()
                    .question(questionDto)
                    .answer(answerDto)
                    .build();
            });
    }

    public Mono<ServiceErrorCreateResponseDto> postServiceError(ServiceErrorCreateRequestDto request) {
        return serviceErrorRepository.save(
                ServiceErrorEntity.builder()
                    .content(request.content())
                    .title(request.title())
                    .tenantId(request.tenantId())
                    .isAnswered(IsAnswered.NOT_ANSWERED)
                    .status(Status.USE)
                    .build())
            .flatMap(savedServiceError -> Mono.just(ServiceErrorCreateResponseDto.builder()
                .errorId(savedServiceError.getErrorId())
                .content(savedServiceError.getContent())
                .title(savedServiceError.getTitle())
                .tenantId(savedServiceError.getTenantId())
                .build())
            );
    }

    public Mono<ServiceErrorCreateResponseDto> patchServiceError(Long errorId, ServiceErrorPatchRequestDto patchDto) {
        return serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> {
                if(serviceError.getIsAnswered().equals(IsAnswered.ANSWERED)) {
                    return Mono.error(
                        new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_CANNOT_MODIFY));
                }
                serviceError.setContent(patchDto.content());
                serviceError.setTitle(patchDto.title());
                return serviceErrorRepository.save(serviceError)
                    .flatMap(savedServiceError -> Mono.just(ServiceErrorCreateResponseDto.builder()
                        .errorId(savedServiceError.getErrorId())
                        .content(savedServiceError.getContent())
                        .title(savedServiceError.getTitle())
                        .tenantId(savedServiceError.getTenantId())
                        .build())
                    );
            });
    }

    public Mono<Void> deleteServiceError(Long errorId) {
        return serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> {
                if(serviceError.getIsAnswered().equals(IsAnswered.ANSWERED)) {
                    return Mono.error(
                        new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_CANNOT_DELETE));
                }
                serviceError.setStatus(Status.DELETED);
                return serviceErrorRepository.save(serviceError);
            })
            .then();
    }
}