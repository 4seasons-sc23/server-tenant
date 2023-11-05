package com.instream.tenant.domain.common.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Status {
    USE("N"),
    DELETED("Y");

    private final String code;

    Status(String code) {
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
                .orElseThrow(() -> new IllegalArgumentException(String.format("상태에 %s가 존재하지 않습니다.", code)));
    }
}

