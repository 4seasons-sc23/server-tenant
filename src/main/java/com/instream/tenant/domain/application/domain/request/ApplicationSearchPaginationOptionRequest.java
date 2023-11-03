package com.instream.tenant.domain.application.domain.request;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApplicationSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "어플리케이션 종류")
    private final ApplicationType type;

    @Schema(description = "어플리케이션 상태")
    private final Status status;

    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime startAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime endAt;

    public ApplicationSearchPaginationOptionRequest(boolean firstView, ApplicationType type, Status status, LocalDateTime startAt, LocalDateTime endAt) {
        super(firstView);
        this.type = type;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
