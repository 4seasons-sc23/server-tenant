package com.instream.tenant.domain.tenant.domain.dto;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@ToString
public class TenantDto {
    @Schema(description = "Tenant Id", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private UUID id;

    @Schema(description = "Tenant 계정", example = "testAccount")
    private String account;

    @Schema(description = "Tenant 이름", example = "이름이에요")
    private String name;

    @Schema(description = "Tenant 전화번호", example = "010-0000-0000")
    private String phoneNumber;

    @Schema(description = "Tenant apiKey", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private String apiKey;

    @Schema(description = "Tenant 상태", example = "N")
    private Status status;

    @QueryProjection
    public TenantDto(UUID id, String account, String name, String phoneNumber, String apiKey, Status status) {
        this.id = id;
        this.account = account;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.apiKey = apiKey;
        this.status = status;
    }
}
