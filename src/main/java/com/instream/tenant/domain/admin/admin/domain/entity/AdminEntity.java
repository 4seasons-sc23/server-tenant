package com.instream.tenant.domain.admin.admin.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Table(name = "admins")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminEntity implements RedisEntity {
    @Id
    @Column(value = "admin_id")
    private Long id;

    private String account;

    private String password;

    private Status status;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
