package com.instream.tenant.domain.common.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SortOption {
    ASC("ASC"),
    DESC("DESC");

    private final String code;

    SortOption(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static SortOption fromCode(String code) {
        return Arrays.stream(SortOption.values())
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("정렬 옵션에 %s가 존재하지 않습니다.", code)));
    }
}
