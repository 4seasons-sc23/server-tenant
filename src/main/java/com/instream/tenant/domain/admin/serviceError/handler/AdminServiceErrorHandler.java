package com.instream.tenant.domain.admin.serviceError.handler;

import com.instream.tenant.domain.admin.serviceError.domain.request.ServiceErrorAnswerRequestDto;
import com.instream.tenant.domain.admin.serviceError.service.AdminServiceErrorService;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.serviceError.infra.enums.ServiceErrorErrorCode;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AdminServiceErrorHandler {
    private final AdminServiceErrorService adminServiceErrorService;

    public AdminServiceErrorHandler(AdminServiceErrorService adminServiceErrorService) {
        this.adminServiceErrorService = adminServiceErrorService;
    }

    public Mono<ServerResponse> postServiceErrorAnswer(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));
        // TODO 관리자인지 확인하는 로직 추가

        return request.bodyToMono(ServiceErrorAnswerRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(answerPostDto -> adminServiceErrorService.postServiceErrorAnswer(answerPostDto, errorId))
            .flatMap(serviceErrorAnswerDto -> ServerResponse.status(HttpStatus.CREATED)
                .bodyValue(serviceErrorAnswerDto));
    }

    public Mono<ServerResponse> patchServiceErrorAnswer(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));
        // TODO 관리자인지 확인하는 로직 추가

        return request.bodyToMono(ServiceErrorAnswerRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(answerPostDto -> adminServiceErrorService.patchServiceErrorAnswer(answerPostDto, errorId))
            .flatMap(serviceErrorAnswerDto -> ServerResponse.status(HttpStatus.OK)
                .bodyValue(serviceErrorAnswerDto));
    }

    public Mono<ServerResponse> getServiceErrorDetail(ServerRequest request) {
        Long errorId = Long.valueOf(request.pathVariable("errorId"));
        return adminServiceErrorService.getServiceErrorById(errorId)
            .flatMap(error -> ServerResponse.ok().bodyValue(error))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
}
