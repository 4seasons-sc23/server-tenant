package com.instream.tenant.domain.serviceError.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ServiceErrorErrorCode implements HttpErrorCode {
    SERVICE_ERROR_NOT_FOUND("문의내역을 찾지 못 했습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    ServiceErrorErrorCode(String message, HttpStatus httpStatus) {
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
