package com.instream.tenant.domain.participant.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ParticipantActionTypeCode {
    message("MESSAGE");
    private final String code;

    ParticipantActionTypeCode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ParticipantActionTypeCode fromCode(String code) {
        return Arrays.stream(ParticipantActionTypeCode.values())
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("사용자 행동 종류에 %s가 존재하지 않습니다.", code)));
    }
}
