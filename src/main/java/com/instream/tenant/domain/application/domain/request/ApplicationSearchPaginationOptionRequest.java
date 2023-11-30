package com.instream.tenant.domain.application.domain.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.SortOption;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@ToString(callSuper = true)
@SuperBuilder
public class ApplicationSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "어플리케이션 종류")
    private final ApplicationType type;

    @Schema(description = "어플리케이션 상태")
    private final Status status;

    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime startAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime endAt;

    public ApplicationSearchPaginationOptionRequest(int page, int size, List<SortOptionRequest> sort, boolean firstView, ApplicationType type, Status status, LocalDateTime startAt, LocalDateTime endAt) {
        super(page, size, sort, firstView);
        this.type = type;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Mono<ApplicationSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));

            if (page < 0 || size <= 0) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            ApplicationType type = Optional.ofNullable(queryParams.getFirst("type"))
                    .map(ApplicationType::fromCode)
                    .orElse(null);
            Status status = Optional.ofNullable(queryParams.getFirst("status"))
                    .map(Status::fromCode)
                    .orElse(null);
            LocalDateTime startAt = parseDateTime(queryParams.getFirst("startAt"));
            LocalDateTime endAt = parseDateTime(queryParams.getFirst("endAt"));
            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);
            ApplicationSearchPaginationOptionRequest searchParams = new ApplicationSearchPaginationOptionRequest(page, size, sortOptionRequestList, firstView, type, status, startAt, endAt);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
