package com.instream.tenant.domain.application.domain.entity;

import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.redis.domain.entity.RedisEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
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


    public Mono<ApplicationEntity> validateApiKey(String apiKey) {
        if(Objects.equals(this.apiKey, apiKey)) {
            return Mono.just(this);
        }
        return Mono.error(new RestApiException(CommonHttpErrorCode.UNAUTHORIZED));
    }

    public Mono<ApplicationEntity> toMonoWhenIsOn() {
        if(isOn()) {
            return Mono.just(this);
        }
        if(canNotUse()) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_CAN_NOT_MODIFY));
        }
        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
    }

    public Mono<ApplicationEntity> toMonoWhenIsOff() {
        if(isOff()) {
            return Mono.just(this);
        }
        if(canNotUse()) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_CAN_NOT_MODIFY));
        }
        return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
    }

    public boolean isOn() {
        return status == Status.USE;
    }

    public boolean isOff() {
        return status == Status.PENDING;
    }

    public boolean canNotUse() {
        return status == Status.FORCE_STOPPED || status == Status.DELETED;
    }
}
