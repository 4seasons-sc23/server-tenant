package com.instream.tenant.domain.application.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ApplicationSessionErrorCode implements HttpErrorCode {
    APPLICATION_SESSION_NOT_FOUND("어플리케이션 세션을 찾지 못 했습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    ApplicationSessionErrorCode(String message, HttpStatus httpStatus) {
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
