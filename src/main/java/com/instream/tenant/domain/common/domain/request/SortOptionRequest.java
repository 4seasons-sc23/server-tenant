package com.instream.tenant.domain.common.domain.request;

import com.instream.tenant.domain.common.infra.enums.SortOption;

public record SortOptionRequest(
        String name,

        SortOption option
) {
}
