package com.instream.tenant.domain.participant.domain.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@ToString
public class ParticipantJoinSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final String nickname;

    @Schema(description = "생성 날짜 기준 조회 시작 날짜")
    private final LocalDateTime createdStartAt;

    @Schema(description = "생성 날짜 기준 조회 종료 날짜")
    private final LocalDateTime createdEndAt;

    @Schema(description = "종료 날짜 기준 조회 시작 날짜")
    private final LocalDateTime deletedStartAt;

    @Schema(description = "종료 날짜 기준 조회 종료 날짜")
    private final LocalDateTime deletedEndAt;


    public ParticipantJoinSearchPaginationOptionRequest(int page, int size, List<SortOptionRequest> sort, boolean firstView, String nickname, LocalDateTime createdStartAt, LocalDateTime createdEndAt, LocalDateTime deletedStartAt, LocalDateTime deletedEndAt) {
        super(page, size, sort, firstView);
        this.nickname = nickname;
        this.createdStartAt = createdStartAt;
        this.createdEndAt = createdEndAt;
        this.deletedStartAt = deletedStartAt;
        this.deletedEndAt = deletedEndAt;
    }

    public static Mono<ParticipantJoinSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));

            if (page < 0 || size <= 0) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            String nickname = queryParams.getFirst("nickname");
            LocalDateTime createdStartAt = parseDateTime(queryParams.getFirst("createdStartAt"));
            LocalDateTime createdEndAt = parseDateTime(queryParams.getFirst("createdEndAt"));
            LocalDateTime deletedStartAt = parseDateTime(queryParams.getFirst("deletedStartAt"));
            LocalDateTime deletedEndAt = parseDateTime(queryParams.getFirst("deletedEndAt"));
            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);

            ParticipantJoinSearchPaginationOptionRequest searchParams = new ParticipantJoinSearchPaginationOptionRequest(page, size, sortOptionRequestList, firstView, nickname, createdStartAt, createdEndAt, deletedStartAt, deletedEndAt);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
