package com.instream.tenant.domain.media.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.instream.tenant.domain.application.service.ApplicationService;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequest;
import com.instream.tenant.domain.media.domain.request.NginxRtmpRequest;
import com.instream.tenant.domain.media.service.MediaService;
import com.instream.tenant.domain.minio.MinioService;

import java.net.URI;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class MediaHandler {
    private final MediaService mediaService;

    private final ApplicationService applicationService;
    private final MinioService minioService;


    public MediaHandler(MediaService mediaService, ApplicationService applicationService, MinioService minioService) {
        this.mediaService = mediaService;
        this.applicationService = applicationService;
        this.minioService = minioService;
    }

    public Mono<ServerResponse> startNginxRtmpStream(ServerRequest request) {
        return request.bodyToMono(NginxRtmpRequest.class)
                .flatMap(applicationService::startApplicationSession)
                .flatMap(applicationSessionDto -> ServerResponse.created(URI.create("")).bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> endNginxRtmpStream(ServerRequest request) {
        return request.bodyToMono(NginxRtmpRequest.class)
                .flatMap(applicationService::endApplicationSession)
                .flatMap(applicationSessionDto -> ServerResponse.ok().bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> uploadMedia(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
                .flatMap(stringPartMultiValueMap -> {
                    Map<String, Part> partMap = stringPartMultiValueMap.toSingleValueMap();
                    return Mono.just(MediaUploadRequest.builder()
                            .m3u8Main((FilePart) partMap.get("m3u8Main"))
                            .m3u8((FilePart) partMap.get("m3u8"))
                            .ts((FilePart) partMap.get("ts"))
                            .quality(request.pathVariable("quality"))
                            .build());
                })
                .flatMap(uploadRequest -> mediaService.uploadMedia(uploadRequest, request.headers().firstHeader(InstreamHttpHeaders.API_KEY)))
                .flatMap(result -> ok().bodyValue(result));
    }
}
