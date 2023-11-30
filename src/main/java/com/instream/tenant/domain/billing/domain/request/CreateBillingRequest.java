package com.instream.tenant.domain.billing.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CreateBillingRequest(
        @Schema(description = "어플리케이션 세션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
        UUID sessionId,

        @Schema(description = "사용량에 따른 비용", example = "123123.123123")
        double cost
) {
}
