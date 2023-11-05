package com.instream.tenant.domain.participant.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "PARTICIPANT_JOINS")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantJoinEntity implements RedisEntity {
    @Id
    @Column(value = "participant_join_id")
    private UUID id;

    private UUID participantId;

    private UUID tenantId;

    private UUID sessionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
