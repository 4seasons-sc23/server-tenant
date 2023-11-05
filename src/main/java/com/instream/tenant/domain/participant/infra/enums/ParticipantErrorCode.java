package com.instream.tenant.domain.participant.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springframework.http.HttpStatus;

public enum ParticipantErrorCode implements HttpErrorCode {
    PARTICIPANT_NOT_FOUND("참가자를 찾지 못 했습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    ParticipantErrorCode(String message, HttpStatus httpStatus) {
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
