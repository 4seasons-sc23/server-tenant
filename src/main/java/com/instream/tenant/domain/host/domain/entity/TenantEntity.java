package com.instream.tenant.domain.host.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Table(name = "TENANTS")
public class TenantEntity {
    @Id
    private UUID id;

    private String account;

    private String password;

    private Status status;

    private String name;

    private String phoneNum;

    private String secretKey;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
