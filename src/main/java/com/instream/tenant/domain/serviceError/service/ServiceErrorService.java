package com.instream.tenant.domain.serviceError.service;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorAnswerEntity;
import com.instream.tenant.domain.serviceError.domain.entity.ServiceErrorEntity;
import com.instream.tenant.domain.serviceError.domain.request.ServiceErrorCreateRequestDto;
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
        Mono<ServiceErrorEntity> errorEntityMono = serviceErrorRepository.findByErrorId(errorId)
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

                ServiceErrorAnswerDto answerDto = "Y".equals(question.getIsAnswered()) ? null :
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
//            .flatMap(serviceError -> Mono.just(ServiceErrorQuestionDto.builder()
//                .errorId(serviceError.getErrorId())
//                .title(serviceError.getTitle())
//                .content(serviceError.getContent())
//                .tenantId(serviceError.getTenantId())
//                .isAnswered(serviceError.getIsAnswered())
//                .status(serviceError.getStatus())
//                .createdAt(serviceError.getCreatedAt())
//                .updatedAt(serviceError.getUpdatedAt())
//                .build())
//                )
//            .switchIfEmpty(
//                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)));
//
//        // Function 기반 Mono
//        Function<Long, Mono<ServiceErrorAnswerDto>> serviceErrorEntityMonoFunction
//            = serviceErrorId -> serviceErrorAnswerRepository.findByErrorId(serviceErrorId)
//            .flatMap(serviceErrorAnswerEntity -> Mono.just(ServiceErrorAnswerDto.builder().build()));
//
//        // 위에 체인 기반 Mono
//        Mono<ServiceErrorDetailDto> serviceErrorAnswerDtoMono = questionDtoMono
//            .flatMap(serviceErrorDetailDto -> serviceErrorAnswerRepository.findByErrorId(errorId)
//            .flatMap(answerEntity -> Mono.just(ServiceErrorAnswerDto.builder()
//                    .answerId(answerEntity.getAnswerId())
//                    .content(answerEntity.getContent())
//                    .status(answerEntity.getStatus())
//                    .createdAt(answerEntity.getCreatedAt())
//                    .updatedAt(answerEntity.getUpdatedAt())
//                    .build()
//                )
//            ));


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
}