package com.instream.tenant.domain.tenant.domain.dto;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@ToString
public class TenantDto {
    private UUID id;

    private String account;

    private String name;

    private String phoneNumber;

    private Status status;

    @QueryProjection
    public TenantDto(UUID id, String account, String name, String phoneNumber, Status status) {
        this.id = id;
        this.account = account;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }
}
