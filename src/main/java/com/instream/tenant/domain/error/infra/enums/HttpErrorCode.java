package com.instream.tenant.domain.error.infra.enums;

import com.fasterxml.jackson.annotation.*;
import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface HttpErrorCode {
    @JsonIgnore
    HttpStatus getHttpStatus();

    @JsonProperty("code")
    String getCode();

    @JsonProperty("message")
    String getMessage();
}
