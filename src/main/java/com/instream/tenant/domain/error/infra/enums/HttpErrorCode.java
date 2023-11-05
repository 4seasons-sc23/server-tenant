package com.instream.tenant.domain.error.infra.enums;

import org.springframework.http.HttpStatus;

public interface HttpErrorCode {

    HttpStatus getHttpStatus();

    String getCode();

    String getMessage();
}
