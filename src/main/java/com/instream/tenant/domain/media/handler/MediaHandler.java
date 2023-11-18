package com.instream.tenant.domain.media.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequestDto;
import com.instream.tenant.domain.media.service.MediaService;
import com.instream.tenant.domain.minio.MinioService;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
    private final MinioService minioService;


    public MediaHandler(MediaService mediaService, MinioService minioService) {
        this.mediaService = mediaService;
        this.minioService = minioService;
    }

    public Mono<ServerResponse> uploadMedia(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
                .flatMap(stringPartMultiValueMap -> {
                    Map<String, Part> partMap = stringPartMultiValueMap.toSingleValueMap();
                    return Mono.just(MediaUploadRequestDto.builder()
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
