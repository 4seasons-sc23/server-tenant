package com.instream.tenant.domain.media.service;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequestDto;
import com.instream.tenant.domain.minio.MinioService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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


                if (uploadRequest.getM3u8Main() == null) {
                    return Mono.zip(
                        minioService.uploadFile(savedPath + "/" + uploadRequest.getQuality() + "/" + "index.m3u8",
                            uploadRequest.getM3u8(), "application/vnd.apple.mpegurl"),
                        minioService.uploadFile(savedPath + "/" + uploadRequest.getQuality() + "/" + uploadRequest.getTs().filename(),
                            uploadRequest.getTs(), "video/MP2T")
                    );
                }
                return Mono.zip(
                    minioService.uploadFile(savedPath + "/index.m3u8", uploadRequest.getM3u8Main(),
                        "application/vnd.apple.mpegurl"),
                    minioService.uploadFile(savedPath + "/" + uploadRequest.getQuality() + "/" + "index.m3u8",
                        uploadRequest.getM3u8(), "application/vnd.apple.mpegurl"),
                    minioService.uploadFile(savedPath + "/" + uploadRequest.getQuality() + "/" + uploadRequest.getTs().filename(),
                        uploadRequest.getTs(), "video/MP2T")
                );
            }).thenReturn("File uploaded successfully");
    }

}
