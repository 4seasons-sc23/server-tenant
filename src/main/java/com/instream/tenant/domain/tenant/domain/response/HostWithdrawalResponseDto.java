package com.instream.tenant.domain.tenant.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.instream.tenant.domain.common.infra.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

public record HostWithdrawalResponseDto(
    @Schema(example = "80bd6328-76a7-11ee-b720-0242ac130003", description = "사용자 인덱스 번호")
    UUID hostId,

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(example = "2023-06-30T09:53:33.393Z", description = "회원 탈퇴 시각")
    LocalDateTime deletedAt,

    @Schema(example = "F", description = "사용자 계정 상태")
    Status status
) {
    @Builder
    public HostWithdrawalResponseDto{}
}
