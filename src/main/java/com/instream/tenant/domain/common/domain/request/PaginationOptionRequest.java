package com.instream.tenant.domain.common.domain.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.instream.tenant.domain.common.infra.enums.SortOption;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import reactor.core.publisher.Mono;

@Getter
@AllArgsConstructor
@ToString
public class PaginationOptionRequest {
    @Schema(description = "현재 페이지")
    private final int page;

    @Schema(description = "요청 사이즈")
    private final int size;

    @Schema(description = "정렬 옵션")
    private final List<SortOptionRequest> sort;

    @Schema(description = "페이지 데이터를 처음 불러오는지 여부")
    private final boolean firstView;

    @JsonIgnore
    public Pageable getPageable() {
        return PageRequest.of(page, size);
    }

    public static Mono<? extends PaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));

            if (page < 0 || size <= 0) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);
            PaginationOptionRequest searchParams = new PaginationOptionRequest(page, size, sortOptionRequestList, firstView);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }


    protected static List<SortOptionRequest> getSortOptionRequestList(MultiValueMap<String, String> queryParams) {
        List<SortOptionRequest> sortOptionRequestList = new ArrayList<>();
        List<String> sortNameList = queryParams.get("sort[name]");
        List<String> sortOptionList = queryParams.get("sort[option]");

        if (!sortNameList.isEmpty() && !sortOptionList.isEmpty()) {
            if (sortNameList.size() != sortOptionList.size()) {
                throw new IllegalArgumentException();
            }

            sortOptionRequestList = IntStream.range(0, sortNameList.size())
                    .mapToObj(index -> new SortOptionRequest(sortNameList.get(index), SortOption.fromCode(sortOptionList.get(index))))
                    .collect(Collectors.toList());
        }
        return sortOptionRequestList;
    }
}
