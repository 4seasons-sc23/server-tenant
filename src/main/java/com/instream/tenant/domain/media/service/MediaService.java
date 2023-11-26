package com.instream.tenant.domain.media.service;

import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.domain.application.repository.ApplicationSessionRepository;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequest;
import com.instream.tenant.domain.minio.MinioService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.stream.Collectors;

import io.minio.ObjectWriteResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${minio.bucket}")
    private String bucketName;

    public MediaService(ApplicationRepository applicationRepository,
                        ApplicationSessionRepository applicationSessionRepository, MinioService minioService) {
        this.applicationRepository = applicationRepository;
        this.applicationSessionRepository = applicationSessionRepository;
        this.minioService = minioService;
    }

    public Mono<String> uploadMedia(MediaUploadRequest uploadRequest, String apiKey) {
        return applicationRepository.findByApiKey(apiKey)
                .flatMap(applicationEntity -> this.getSessionIdByApplicationId(applicationEntity.getId()))
                .flatMap(sessionId -> {
                    String savedPath = sessionId.toString();
                    String m3u8ContentType = "application/vnd.apple.mpegurl";
                    String tsContentType = "video/MP2T";
                    String mainM3u8SavedPath = savedPath + "/index.m3u8";
                    String m3u8SavedPath = savedPath + "/" + uploadRequest.quality() + "/index.m3u8";
                    String tsSavedPath = savedPath + "/" + uploadRequest.quality() + "/" + uploadRequest.ts().filename();
                    Mono<ObjectWriteResponse> uploadMainM3u8 = convertMainM3u8ToFile(uploadRequest.m3u8Main(), apiKey, savedPath)
                            .flatMap(file -> minioService.uploadFile(mainM3u8SavedPath, file, m3u8ContentType));
                    Mono<ObjectWriteResponse> uploadM3u8 = convertToFile(uploadRequest.m3u8(), savedPath)
                            .flatMap(file -> minioService.uploadFile(m3u8SavedPath, file, m3u8ContentType));
                    Mono<ObjectWriteResponse> uploadTs = convertToFile(uploadRequest.ts(), savedPath)
                            .flatMap(file -> minioService.uploadFile(tsSavedPath, file, tsContentType));

                    return Mono.zip(uploadMainM3u8, uploadM3u8, uploadTs);
                })
                .thenReturn("Upload media successfully");
    }

    @NotNull
    private Mono<UUID> getSessionIdByApplicationId(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .flatMap(this::validationStreamingApplication)
                .flatMap(application -> applicationSessionRepository.findTopByApplicationIdAndDeletedAtOrderByCreatedAtDesc(application.getId(), null))
                .switchIfEmpty(Mono.error(new RestApiException(ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND)))
                .flatMap(applicationSessionEntity -> Mono.just(applicationSessionEntity.getId()));
    }

    @NotNull
    private  Mono<ApplicationEntity> validationStreamingApplication(ApplicationEntity application) {
        if(application.getType() != ApplicationType.STREAMING) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_SUPPORTED));
        }
        if(application.getStatus() != Status.USE) {
            return Mono.error(new RestApiException(ApplicationErrorCode.APPLICATION_NOT_ON));
        }
        return Mono.just(application);
    }

    private Mono<File> convertToFile(FilePart filePart, String sessionId) {
        if (filePart == null) {
            return Mono.empty();
        }
        return Mono.create(sink -> {
            try {
                // 임시 파일 생성
                File tempFile = File.createTempFile(String.format("temp_%s", sessionId), filePart.filename());
                WritableByteChannel channel = Channels.newChannel(new FileOutputStream(tempFile));

                // FilePart의 내용을 임시 파일에 쓴다
                filePart.content().subscribe(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        channel.write(ByteBuffer.wrap(bytes));
                    } catch (IOException e) {
                        sink.error(e);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                }, sink::error, () -> {
                    try {
                        channel.close();
                        sink.success(tempFile);
                    } catch (IOException e) {
                        sink.error(e);
                    }
                });
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    private Mono<File> convertMainM3u8ToFile(FilePart filePart, String apiKey, String sessionId) {
        if (filePart == null) {
            return Mono.empty();
        }
        return Mono.create(sink -> {
            try {
                // 임시 파일 생성
                File tempFile = File.createTempFile(String.format("temp_main_%s", sessionId), ".m3u8");

                // FilePart의 내용을 임시 파일에 쓴다
                filePart.content()
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return ByteBuffer.wrap(bytes);
                        })
                        .collectList()
                        .flatMap(list -> Mono.fromCallable(() -> {
                            try (WritableByteChannel channel = Channels.newChannel(new FileOutputStream(tempFile))) {
                                for (ByteBuffer byteBuffer : list) {
                                    channel.write(byteBuffer);
                                }
                            }
                            return tempFile;
                        }))
                        .flatMap(file -> replaceTextInFile(file, String.format("%s_", apiKey), String.format("/%s/%s/", bucketName, sessionId)))
                        .subscribe(sink::success, sink::error);
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    /**
     * m3u8 파일에서 특정 텍스트 부분을 변경하기 위한 함수입니다.
     * <p>
     * mainM3u8 파일이 {api_key}_{quality}/index.m3u8 이렇게 m3u8들의 경로를 잡습니다.
     * 이를 {bucket_name}/{session_id}/index.m3u8 이렇게 변경하기 위한 함수입니다.
     */
    private Mono<File> replaceTextInFile(File file, String searchText, String replaceText) {
        return Mono.fromCallable(() -> {
            Path path = file.toPath();
            String content = Files.lines(path)
                    .map(line -> line.replace(searchText, replaceText))
                    .collect(Collectors.joining("\n"));
            Files.write(path, content.getBytes(), StandardOpenOption.WRITE);
            return file;
        });
    }
}
