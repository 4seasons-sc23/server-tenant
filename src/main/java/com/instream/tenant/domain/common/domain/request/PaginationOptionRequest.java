package com.instream.tenant.domain.common.domain.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.instream.tenant.domain.common.infra.enums.SortOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@AllArgsConstructor
@ToString
@SuperBuilder
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

    protected static List<SortOptionRequest> getSortOptionRequestList(MultiValueMap<String, String> queryParams) {
        List<SortOptionRequest> sortOptionRequestList = new ArrayList<>();
        List<String> sortNameList = queryParams.get("sort[name]");
        List<String> sortOptionList = queryParams.get("sort[option]");

        if (sortNameList == null || sortOptionList == null ){
            return sortOptionRequestList;
        }
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

    protected static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
    }
}
