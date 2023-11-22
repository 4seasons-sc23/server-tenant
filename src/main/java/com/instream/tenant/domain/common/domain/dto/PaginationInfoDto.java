package com.instream.tenant.domain.common.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PaginationInfoDto<T extends CollectionDto> extends PaginationDto<T> {
    @Schema(description = "총 페이지 개수, 0부터 시작")
    @Min(value = 0)
    long pageCount;

    @Schema(description = "총 개수, 0부터 시작")
    @Min(value = 0)
    int totalElementCount;
}
