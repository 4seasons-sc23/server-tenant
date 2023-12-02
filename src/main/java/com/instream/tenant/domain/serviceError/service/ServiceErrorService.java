package com.instream.tenant.domain.serviceError.service;

import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.common.domain.dto.CollectionDto;
import com.instream.tenant.domain.common.domain.dto.PaginationDto;
import com.instream.tenant.domain.common.domain.dto.PaginationInfoDto;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.domain.dto.ServiceErrorDto;
import com.instream.tenant.domain.serviceError.domain.entity.QServiceErrorEntity;
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
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
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
        Mono<ServiceErrorEntity> errorEntityMono = serviceErrorRepository.findByErrorIdAndStatus(
                errorId, Status.USE)
            .switchIfEmpty(
                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)));

        Mono<ServiceErrorAnswerEntity> answerEntityMono = serviceErrorAnswerRepository.findByErrorId(
                errorId)
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

                ServiceErrorAnswerDto answerDto =
                    IsAnswered.NOT_ANSWERED.equals(question.getIsAnswered()) ? null :
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

    public Mono<ServiceErrorCreateResponseDto> postServiceError(
        ServiceErrorCreateRequestDto request) {
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

    public Mono<ServiceErrorCreateResponseDto> patchServiceError(Long errorId,
        ServiceErrorPatchRequestDto patchDto) {
        return serviceErrorRepository.findByErrorIdAndStatus(errorId, Status.USE)
            .switchIfEmpty(
                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> {
                if (serviceError.getIsAnswered().equals(IsAnswered.ANSWERED)) {
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
            .switchIfEmpty(
                Mono.error(new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_NOT_FOUND)))
            .flatMap(serviceError -> {
                if (serviceError.getIsAnswered().equals(IsAnswered.ANSWERED)) {
                    return Mono.error(
                        new RestApiException(ServiceErrorErrorCode.SERVICE_ERROR_CANNOT_DELETE));
                }
                serviceError.setStatus(Status.DELETED);
                return serviceErrorRepository.save(serviceError);
            })
            .then();
    }

    public Mono<PaginationDto<CollectionDto<ServiceErrorQuestionDto>>> getServiceErrorsByHostId(UUID hostId,
        PaginationOptionRequest paginationOptionRequest) {
        Pageable pageable = paginationOptionRequest.getPageable();

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(QServiceErrorEntity.serviceErrorEntity.tenantId.eq(Expressions.constant(hostId.toString())));
        booleanBuilder.and(QServiceErrorEntity.serviceErrorEntity.status.eq(Expressions.constant(Status.USE.getCode())));

        Flux<ServiceErrorEntity> serviceErrorEntityFlux = serviceErrorRepository.query(sqlQuery -> sqlQuery
            .select(QServiceErrorEntity.serviceErrorEntity)
            .from(QServiceErrorEntity.serviceErrorEntity)
            .where(booleanBuilder)
            .orderBy(QServiceErrorEntity.serviceErrorEntity.createdAt.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
        ).all();

        Flux<ServiceErrorQuestionDto> serviceErrorDtoFlux = serviceErrorEntityFlux
            .doOnNext(serviceError -> {
                System.out.println("Mapping ServiceErrorEntity to ServiceErrorQuestionDto: " + serviceError);
            })
            .flatMap(serviceError -> Mono.just(ServiceErrorQuestionDto.builder()
                .errorId(serviceError.getErrorId())
                .title(serviceError.getTitle())
                .tenantId(serviceError.getTenantId())
                .content(serviceError.getContent())
                .isAnswered(serviceError.getIsAnswered())
                .status(serviceError.getStatus())
                .createdAt(serviceError.getCreatedAt())
                .build()));

        if(paginationOptionRequest.isFirstView()) {
            return serviceErrorDtoFlux.collectList()
                .flatMap(serviceErrorDtoList -> serviceErrorRepository
                    .count(booleanBuilder)
                    .flatMap(count -> {
                        int pageCount = (int) Math.ceil((double) count / pageable.getPageSize());
                        return Mono.just(PaginationInfoDto.<CollectionDto<ServiceErrorQuestionDto>>builder()
                            .totalElementCount(count)
                            .pageCount(pageCount)
                            .currentPage(paginationOptionRequest.getPage())
                            .data(CollectionDto.<ServiceErrorQuestionDto>builder()
                                .data(serviceErrorDtoList)
                                .build())
                            .build());
                    }));
        }

        return serviceErrorDtoFlux.collectList()
            .flatMap(serviceErrorDtoList -> Mono.just(PaginationDto.<CollectionDto<ServiceErrorQuestionDto>>builder()
                .currentPage(paginationOptionRequest.getPageable().getPageNumber())
                .data(CollectionDto.<ServiceErrorQuestionDto>builder()
                    .data(serviceErrorDtoList)
                    .build())
                .build()));
    }
}