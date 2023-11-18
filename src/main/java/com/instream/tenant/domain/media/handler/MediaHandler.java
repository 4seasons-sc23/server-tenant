package com.instream.tenant.domain.media.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
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
            .map(map -> {
                var parts = map.toSingleValueMap();
                MediaUploadRequestDto uploadRequest = new MediaUploadRequestDto();
                if (parts.get("m3u8Main") instanceof FilePart) {
                    uploadRequest.setM3u8Main((FilePart) parts.get("m3u8Main"));
                }
                if (parts.get("m3u8") instanceof FilePart) {
                    uploadRequest.setM3u8((FilePart) parts.get("m3u8"));
                }
                if (parts.get("ts") instanceof FilePart) {
                    uploadRequest.setTs((FilePart) parts.get("ts"));
                }
                uploadRequest.setQuality(request.pathVariable("quality"));
                return uploadRequest;
            })
            .flatMap(uploadRequest -> mediaService.uploadMedia(uploadRequest, request.headers().header("api_key").get(0)))
            .flatMap(result -> ok().bodyValue(result));
    }
}
