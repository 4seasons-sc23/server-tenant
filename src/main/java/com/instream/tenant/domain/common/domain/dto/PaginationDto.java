package com.instream.tenant.domain.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PaginationDto<T extends CollectionDto>{
    @Schema(description = "현재 페이지, 1부터 시작")
    @Min(value = 0)
    int currentPage;

    @Schema(description = "데이터")
    @NotNull
    @JsonUnwrapped
    T data;
}
