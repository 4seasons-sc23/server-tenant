package com.instream.tenant.domain.application.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ApplicationErrorCode implements HttpErrorCode {
    APPLICATION_NOT_FOUND("어플리케이션을 찾지 못 했습니다.", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_SUPPORTED("해당 어플리케이션 종류는 지원하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

    ApplicationErrorCode(String message, HttpStatus httpStatus) {
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
