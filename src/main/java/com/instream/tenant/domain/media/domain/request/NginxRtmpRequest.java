package com.instream.tenant.domain.media.domain.request;

public record NginxRtmpRequest(
        String addr,
        String app,
        String flashver,
        String swfurl,
        String tcurl,
        String pageurl,
        String name
) {
}
