package com.instream.tenant.domain.tenant.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import lombok.*;
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
@ToString
public class TenantEntity implements RedisEntity {
    @Id
    @Column(value = "tenant_id")
    private UUID id;

    private String account;

    private String password;

    private Status status;

    private String name;

    private String phoneNumber;

    private String secretKey;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
