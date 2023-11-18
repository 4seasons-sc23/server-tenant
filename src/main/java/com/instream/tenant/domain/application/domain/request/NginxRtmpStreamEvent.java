package com.instream.tenant.domain.application.domain.request;

public record NginxRtmpStreamEvent(
        String addr,

        String app,

        String flashver,

        String swfurl,

        String tcurl,

        String pageurl,

        String name
) {
}
