package com.instream.tenant.domain.tenant.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum TenantErrorCode implements HttpErrorCode {
    TENANT_NOT_FOUND("호스트를 찾지 못 했습니다.", HttpStatus.NOT_FOUND),
    EXIST_TENANT("존재하는 호스트입니다.", HttpStatus.CONFLICT),
    EXIST_ACCOUNT("존재하는 아이디입니다.", HttpStatus.CONFLICT),

    UNAUTHORIZED("계정 혹은 비밀번호가 맞지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_PHONE_NUM_FORMAT_ERROR( "전화번호는 000-0000-0000 형식에 맞춰서 입력해주세요.", HttpStatus.BAD_REQUEST);


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
