package com.instream.tenant.domain.common.domain.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationOptionRequest {
    @Schema(description = "페이지 데이터를 처음 불러오는지 여부")
    private final boolean firstView;
}
