package com.instream.tenant.domain.application.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record NginxRtmpStreamEvent(
        @Schema()
        String addr,

        @Schema()
        String app,

        @Schema()
        String flashver,

        @Schema()
        String swfurl,

        @Schema()
        String tcurl,

        @Schema()
        String pageurl,

        @Schema()
        String name
) {
}
