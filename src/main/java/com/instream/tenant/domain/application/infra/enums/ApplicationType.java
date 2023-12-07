package com.instream.tenant.domain.application.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ApplicationType {
    CHAT("CHAT"),
    STREAMING("STREAMING"),
    LIVE("LIVE");

    private final String code;

    ApplicationType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ApplicationType fromCode(String code) {
        return Arrays.stream(ApplicationType.values())
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("어플리케이션 종류에 %s가 존재하지 않습니다.", code)));
    }
}

