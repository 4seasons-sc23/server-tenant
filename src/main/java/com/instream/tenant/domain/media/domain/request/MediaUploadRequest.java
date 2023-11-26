package com.instream.tenant.domain.media.domain.request;


import lombok.Builder;

import org.springframework.http.codec.multipart.FilePart;


public record MediaUploadRequest(
        FilePart m3u8Main,

        FilePart m3u8,

        FilePart ts,

        String quality
) {
    @Builder
    public MediaUploadRequest {

    }

}
