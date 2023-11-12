package com.instream.tenant.domain.application.domain.entity;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "applications")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApplicationEntity implements RedisEntity {
    @Id
    @Column(value = "application_id")
    private UUID id;

    private UUID tenantId;

    private String apiKey;

    private ApplicationType type;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
