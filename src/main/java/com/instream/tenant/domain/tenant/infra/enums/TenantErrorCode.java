package com.instream.tenant.domain.tenant.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum TenantErrorCode implements HttpErrorCode {
    TENANT_NOT_FOUND("호스트를 찾지 못 했습니다.", HttpStatus.NOT_FOUND),
    EXIST_TENANT("존재하는 호스트입니다.", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;

    TenantErrorCode(String message, HttpStatus httpStatus) {
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