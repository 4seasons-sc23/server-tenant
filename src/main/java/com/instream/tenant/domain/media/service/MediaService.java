package com.instream.tenant.domain.media.service;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequestDto;
import com.instream.tenant.domain.minio.MinioService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MediaService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationSessionRepository applicationSessionRepository;
    private final MinioService minioService;


    public MediaService(ApplicationRepository applicationRepository,
                        ApplicationSessionRepository applicationSessionRepository, MinioService minioService) {
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.minioService = minioService;
    }

    public Mono<UUID> getSessionIdByApplicationId(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .flatMap(app -> applicationSessionRepository
                        .findTopByApplicationIdAndDeletedAtOrderByCreatedAtDesc(app.getId(), null)
                )
                .flatMap(applicationSessionEntity -> Mono.just(applicationSessionEntity.getId()));
    }

    public Mono<String> uploadMedia(MediaUploadRequestDto uploadRequest, String apiKey) {
        return applicationRepository.findByApiKey(apiKey)
                .flatMap(
                        applicationEntity -> this.getSessionIdByApplicationId(applicationEntity.getId()))
                .flatMap(sessionId -> {
                    String savedPath = sessionId.toString();

                    log.debug("uploadRequest {}, apiKey: {}", uploadRequest, apiKey);

                    if (uploadRequest.quality() == null) {
                        return Mono.just("quality is null");
                    }
                    if (uploadRequest.ts() == null) {
                        return Mono.just("ts is null");
                    }

                    Mono<String> uploadMainM3u8 = minioService.uploadFile(savedPath + "/index.m3u8", uploadRequest.m3u8Main(),
                            "application/vnd.apple.mpegurl");
                    Mono<String> uploadM3u8 = minioService.uploadFile(savedPath + "/" + uploadRequest.quality() + "/" + "index.m3u8",
                            uploadRequest.m3u8(), "application/vnd.apple.mpegurl");
                    Mono<String> uploadTs = minioService.uploadFile(savedPath + "/" + uploadRequest.quality() + "/" + uploadRequest.ts().filename(),
                            uploadRequest.ts(), "video/MP2T");

                    return Mono.zip(uploadMainM3u8, uploadM3u8, uploadTs);
                })
                .thenReturn("File uploaded successfully");
    }
}
