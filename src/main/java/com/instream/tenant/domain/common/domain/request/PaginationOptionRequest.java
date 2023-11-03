package com.instream.tenant.domain.common.domain.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
public class PaginationOptionRequest {
    @Schema(description = "현재 페이지")
    private final int page;

    @Schema(description = "페이지 사이즈")
    private final int size;

    @Schema(description = "페이지 데이터를 처음 불러오는지 여부")
    private final boolean firstView;

    @JsonIgnore
    public Pageable getPageable() {
        return PageRequest.of(page, size);
    }
}
