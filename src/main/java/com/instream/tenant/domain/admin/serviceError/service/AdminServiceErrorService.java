package com.instream.tenant.domain.admin.serviceError.service;

import com.instream.tenant.domain.admin.serviceError.domain.request.ServiceErrorAnswerRequestDto;
import com.instream.tenant.domain.admin.serviceError.domain.response.AdminServiceErrorDetail;
import com.instream.tenant.domain.admin.serviceError.domain.response.ServiceErrorWriterDto;
import com.instream.tenant.domain.admin.serviceError.infra.enums.ServiceErrorAnswerErrorCode;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorAnswerEntity;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorEntity;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorAnswerDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorDetailDto;
import com.instream.tenant.domain.serviceError.domain.response.ServiceErrorQuestionDto;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import com.instream.tenant.domain.serviceError.infra.enums.ServiceErrorErrorCode;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorAnswerRepository;
import com.instream.tenant.domain.serviceError.repository.ServiceErrorRepository;
import com.instream.tenant.domain.tenant.domain.entity.TenantEntity;
import com.instream.tenant.domain.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AdminServiceErrorService {
    private final ServiceErrorRepository serviceErrorRepository;
    private final ServiceErrorAnswerRepository serviceErrorAnswerRepository;
    private final TenantRepository tenantRepository;

    public AdminServiceErrorService(ServiceErrorRepository serviceErrorRepository,
        ServiceErrorAnswerRepository serviceErrorAnswerRepository,
        TenantRepository tenantRepository) {
        this.serviceErrorRepository = serviceErrorRepository;
        this.serviceErrorAnswerRepository = serviceErrorAnswerRepository;
        this.tenantRepository = tenantRepository;
    }

    public Mono<ServiceErrorAnswerDto> postServiceErrorAnswer(
        ServiceErrorAnswerRequestDto request, Long errorId) {
        return serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> {
                if(serviceError.getIsAnswered().equals(IsAnswered.ANSWERED)) {
                    return Mono.error(
                        new RestApiException(ServiceErrorAnswerErrorCode.SERVICE_ERROR_ANSWER_EXIST)
                    );
                }
                serviceError.setIsAnswered(IsAnswered.ANSWERED);
                return serviceErrorRepository.save(serviceError);
            })
            .flatMap(serviceError -> serviceErrorAnswerRepository.save(
                    ServiceErrorAnswerEntity.builder()
                        .errorId(serviceError.getErrorId())
                        .content(request.answerContent())
                        .status(Status.USE)
                        .build())
                .flatMap(savedServiceError -> Mono.just(ServiceErrorAnswerDto.builder()
                    .content(savedServiceError.getContent())
                    .status(savedServiceError.getStatus())
                    .createdAt(savedServiceError.getCreatedAt())
                    .build())
                ));
    }

    public Mono<ServiceErrorAnswerDto> patchServiceErrorAnswer(
        ServiceErrorAnswerRequestDto request, Long errorId) {
        return serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(
                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> serviceErrorAnswerRepository.findByErrorId(serviceError.getErrorId())
                .switchIfEmpty(Mono.error(
                    new RestApiException(ServiceErrorAnswerErrorCode.SERVICE_ERROR_ANSWER_NOT_FOUND))
                ))
            .flatMap(serviceErrorAnswer -> {
                serviceErrorAnswer.setContent(request.answerContent());
                return serviceErrorAnswerRepository.save(serviceErrorAnswer)
                    .flatMap(modifiedAnswer -> Mono.just(ServiceErrorAnswerDto.builder()
                        .content(modifiedAnswer.getContent())
                        .status(modifiedAnswer.getStatus())
                        .createdAt(modifiedAnswer.getCreatedAt())
                        .build()));
            });
    }

    public Mono<AdminServiceErrorDetail> getServiceErrorById(Long errorId) {
        Mono<ServiceErrorEntity> errorEntityMono = serviceErrorRepository.findByErrorIdAndStatus(
                errorId, Status.USE)
            .switchIfEmpty(
                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)));

        Mono<TenantEntity> writerEntityMono  = errorEntityMono
            .flatMap(error -> tenantRepository.findById(error.getTenantId()))
            .switchIfEmpty(Mono.just(TenantEntity.builder().build()));

        Mono<ServiceErrorAnswerEntity> answerEntityMono = serviceErrorAnswerRepository.findByErrorId(
                errorId)
            .switchIfEmpty(Mono.just(ServiceErrorAnswerEntity.builder().build()));

        return Mono.zip(errorEntityMono, answerEntityMono, writerEntityMono)
            .map(tuple -> {
                ServiceErrorEntity question = tuple.getT1();
                ServiceErrorAnswerEntity answer = tuple.getT2();
                TenantEntity writer = tuple.getT3();

                ServiceErrorWriterDto writerDto = ServiceErrorWriterDto.builder()
                    .tenantId(writer.getId())
                    .userName(writer.getName())
                    .userAccount(writer.getAccount())
                    .build();

                ServiceErrorQuestionDto questionDto = ServiceErrorQuestionDto.builder()
                    .errorId(question.getErrorId())
                    .title(question.getTitle())
                    .tenantId(question.getTenantId())
                    .content(question.getContent())
                    .isAnswered(question.getIsAnswered())
                    .status(question.getStatus())
                    .createdAt(question.getCreatedAt())
                    .build();

                ServiceErrorAnswerDto answerDto =
                    IsAnswered.NOT_ANSWERED.equals(question.getIsAnswered()) ? null :
                        ServiceErrorAnswerDto.builder()
                            .content(answer.getContent())
                            .status(answer.getStatus())
                            .createdAt(answer.getCreatedAt())
                            .build();

                return AdminServiceErrorDetail.builder()
                    .writer(writerDto)
                    .question(questionDto)
                    .answer(answerDto)
                    .build();
            });
    }
}
