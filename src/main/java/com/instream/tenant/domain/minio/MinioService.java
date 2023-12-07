package com.instream.tenant.domain.minio;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.errors.MinioException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public Mono<ObjectWriteResponse> uploadFile(String objectName, File file, String contentType) {
        if (file == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                log.info("Upload file to Minio {}", objectName);
                return minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(fileInputStream, file.length(), 10485760)
                                .contentType(contentType)
                                .build());
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Error uploading file to MinIO", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

//    public Mono<ObjectWriteResponse> deleteFile(String objectName) {
//        if (file == null) {
//            return Mono.empty();
//        }
//
//        return Mono.fromCallable(() -> {
//            try (FileInputStream fileInputStream = new FileInputStream(file)) {
//                log.info("Upload file to Minio {}", objectName);
//                return minioClient.removeObjects(
//                        RemoveObjectsArgs.builder()
//                                .bucket(bucketName)
//                                .objects()
//                                .build());
//            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
//                throw new RuntimeException("Error uploading file to MinIO", e);
//            }
//        }).subscribeOn(Schedulers.boundedElastic());
//    }
}