package com.instream.tenant.domain.serviceError.domain.entity;

import com.instream.tenant.domain.common.infra.enums.Status;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "service_error_answers")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceErrorAnswerEntity {
    @Id
    @Column(value = "answer_id")
    private Long answerId;
    private Long errorId;
    private String content;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
