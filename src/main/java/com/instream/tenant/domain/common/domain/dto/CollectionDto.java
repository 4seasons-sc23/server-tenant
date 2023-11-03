package com.instream.tenant.domain.common.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Collection;

public record CollectionDto<T>(
        @Schema(description = "데이터")
        @NotNull

        Collection<T> data,
        @Schema(description = "데이터 개수")
        @Min(value = 0)
        int length
) {
    @Builder
    public CollectionDto(Collection<T> data, int length) {
        if(data == null) {
            data = new ArrayList<>();
        }

        this.data = data;
        this.length = data.size();
    }
}
