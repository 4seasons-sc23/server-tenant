package com.instream.tenant.domain.admin.serviceError.handler;

import com.instream.tenant.domain.admin.serviceError.domain.request.ServiceErrorAnswerRequestDto;
import com.instream.tenant.domain.admin.serviceError.service.AdminServiceErrorService;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
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
        // TODO 문의내역 존재하는지 확인하는 로직 추가

        return request.bodyToMono(ServiceErrorAnswerRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(answerPostDto -> adminServiceErrorService.postServiceErrorAnswer(answerPostDto, errorId))
            .flatMap(serviceErrorAnswerDto -> ServerResponse.status(HttpStatus.CREATED)
                .bodyValue(serviceErrorAnswerDto));
    }
}
