package com.instream.tenant.domain.application.domain.dto;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationWithApiKeyDto(
        @Schema(description = "어플리케이션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
        UUID id,

        @Schema(description = "어플리케이션 종류", examples = {"CHAT", "STREAMING"})
        ApplicationType type,

        @Schema(description = "어플리케이션 API KEY", example = "80bd6328-76a7-11ee-b720-0242ac130003")
        String apiKey,

        @Schema(description = "어플리케이션 상태", examples = {"N", "Y", "P", "F"})
        Status status,

        @Schema(description = "어플리케이션 생성 시간", example = "2023-11-26T01:59:44.885Z")
        LocalDateTime createdAt,

        ApplicationSessionDto session

) {
    @Builder
    public ApplicationWithApiKeyDto {

    }
}
