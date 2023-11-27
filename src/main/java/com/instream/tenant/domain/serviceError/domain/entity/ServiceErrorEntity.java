package com.instream.tenant.domain.serviceError.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "service_errors")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceErrorEntity {
    @Id
    @Column(value = "error_id")
    private Long errorId;
    private UUID tenantId;
    private String title;
    private String content;
    private IsAnswered isAnswered;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
