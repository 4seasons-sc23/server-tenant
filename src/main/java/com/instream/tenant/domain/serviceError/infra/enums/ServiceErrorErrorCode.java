package com.instream.tenant.domain.serviceError.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ServiceErrorErrorCode implements HttpErrorCode {
    SERVICE_ERROR_NOT_FOUND("존재하지 않는 문의내역입니다.", HttpStatus.NOT_FOUND),
    SERVICE_ERROR_CANNOT_MODIFY("답변 완료된 문의내역은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    SERVICE_ERROR_CANNOT_DELETE("답변 완료된 문의내역은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);

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
