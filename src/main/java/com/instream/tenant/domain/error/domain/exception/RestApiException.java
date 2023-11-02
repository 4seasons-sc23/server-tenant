package com.instream.tenant.domain.error.domain.exception;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class RestApiException extends RuntimeException {
    private final HttpErrorCode httpErrorCode;
}
