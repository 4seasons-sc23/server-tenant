package com.instream.tenant.domain.billing.domain.request;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class BillingSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "어플리케이션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private final UUID applicationId;

    @Schema(description = "어플리케이션 종류")
    private final ApplicationType type;

    @Schema(description = "사용량 상태")
    private final Status status;

    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime createdStartAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime createdEndAt;

    @Schema(description = "종료 날짜 기준 조회 시작 날짜")
    private final LocalDateTime deletedStartAt;

    @Schema(description = "종료 날짜 기준 조회 종료 날짜")
    private final LocalDateTime deletedEndAt;


    public BillingSearchPaginationOptionRequest(int page, int size, List<SortOptionRequest> sort, boolean firstView, UUID applicationId, ApplicationType type, Status status, LocalDateTime createdStartAt, LocalDateTime createdEndAt, LocalDateTime deletedStartAt, LocalDateTime deletedEndAt) {
        super(page, size, sort, firstView);
        this.applicationId = applicationId;
        this.type = type;
        this.status = status;
        this.createdStartAt = createdStartAt;
        this.createdEndAt = createdEndAt;
        this.deletedStartAt = deletedStartAt;
        this.deletedEndAt = deletedEndAt;
    }

    public static Mono<BillingSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));
            Status status = Optional.ofNullable(queryParams.getFirst("status"))
                    .map(Status::fromCode)
                    .orElse(null);
            boolean invalidParameter = page < 0 || size <= 0 || status != null && (status.equals(Status.FORCE_STOPPED) || status.equals(Status.DELETED));

            if (invalidParameter) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            UUID applicationId = Optional.ofNullable(queryParams.getFirst("applicationId"))
                    .map(UUID::fromString)
                    .orElse(null);
            ApplicationType type = Optional.ofNullable(queryParams.getFirst("type"))
                    .map(ApplicationType::fromCode)
                    .orElse(null);
            LocalDateTime createdStartAt = parseDateTime(queryParams.getFirst("createdStartAt"));
            LocalDateTime createdEndAt = parseDateTime(queryParams.getFirst("createdEndAt"));
            LocalDateTime deletedStartAt = parseDateTime(queryParams.getFirst("deletedStartAt"));
            LocalDateTime deletedEndAt = parseDateTime(queryParams.getFirst("deletedEndAt"));
            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);

            BillingSearchPaginationOptionRequest searchParams = new BillingSearchPaginationOptionRequest(page, size, sortOptionRequestList, firstView, applicationId, type, status, createdStartAt, createdEndAt, deletedStartAt, deletedEndAt);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
