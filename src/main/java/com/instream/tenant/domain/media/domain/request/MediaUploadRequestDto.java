package com.instream.tenant.domain.media.domain.request;


import lombok.Getter;
import lombok.Setter;

import org.springframework.http.codec.multipart.FilePart;

@Getter
@Setter
public class MediaUploadRequestDto {
    private FilePart m3u8Main;
    private FilePart m3u8;
    private FilePart ts;
    private String quality;
}
