package com.instream.tenant.domain.common.domain.request;

import com.instream.tenant.domain.common.infra.enums.SortOption;

import java.util.Objects;

public record SortOptionRequest(
        String name,

        SortOption option
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SortOptionRequest that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
