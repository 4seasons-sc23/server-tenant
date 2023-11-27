package com.instream.tenant.domain.application.domain.entity;

import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "application_sessions")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApplicationSessionEntity implements RedisEntity {
    @Id
    @Column(value = "application_session_id")
    private UUID id;

    private UUID applicationId;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }

    public boolean isDeleted () {
        return this.deletedAt != null;
    }
}
