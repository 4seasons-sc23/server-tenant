package com.instream.tenant.domain.serviceError.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.instream.tenant.domain.common.infra.enums.Status;
import java.util.Arrays;

public enum IsAnswered {
    ANSWERED("Y"),
    NOT_ANSWERED("N");

    private final String code;

    IsAnswered(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static Status fromCode(String code) {
        return Arrays.stream(Status.values())
            .filter(v -> v.getCode().equals(code))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException(String.format("답변여부에 %s가 존재하지 않습니다.", code)));
    }}
