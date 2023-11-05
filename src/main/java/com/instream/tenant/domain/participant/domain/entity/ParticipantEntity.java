package com.instream.tenant.domain.participant.domain.entity;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.host.domain.entity.TenantEntity;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "PARTICIPANTS")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantEntity implements RedisEntity {
    @Id
    @Column(value = "participant_id")
    private String id;

    private UUID tenantId;

    @NotBlank
    private String nickname;

    private String profileImgUrl;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
