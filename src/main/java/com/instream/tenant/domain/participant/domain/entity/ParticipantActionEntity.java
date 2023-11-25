package com.instream.tenant.domain.participant.domain.entity;

import com.instream.tenant.domain.participant.infra.enums.ParticipantActionTypeCode;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.*;

@Table(name = "participant_actions")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantActionEntity implements RedisEntity {
    @Id
    @Column(value = "participant_action_id")
    private UUID id;

    private UUID participantJoinId;

    @NotNull
    private ParticipantActionTypeCode type;

    private String data;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String genRedisKey() {
        return getClass().getSimpleName() + "_" + id;
    }
}
