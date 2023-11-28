package com.instream.tenant.domain.admin.serviceError.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ServiceErrorAnswerErrorCode implements HttpErrorCode {

    SERVICE_ERROR_ANSWER_NOT_FOUND("존재하지 않는 문의 답변입니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    ServiceErrorAnswerErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return name();
    }
}

