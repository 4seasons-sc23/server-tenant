package com.instream.tenant.domain.billing.domain.entity;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "billings")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BillingEntity {
    @Id
    @Column(value = "application_id")
    private UUID id;

    private UUID sessionId;

    private Status status;

    private double cost;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
