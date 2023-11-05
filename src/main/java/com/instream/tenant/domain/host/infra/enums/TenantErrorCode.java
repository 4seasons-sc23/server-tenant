package com.instream.tenant.domain.host.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum TenantErrorCode implements HttpErrorCode {
    TENANT_NOT_FOUND("Tenant not found", HttpStatus.NOT_FOUND);

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
