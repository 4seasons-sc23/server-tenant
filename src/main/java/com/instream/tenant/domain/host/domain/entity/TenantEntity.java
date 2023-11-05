package com.instream.tenant.domain.host.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Table(name = "TENANTS")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity {
    @Id
    @Column(value = "tenant_id")
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
