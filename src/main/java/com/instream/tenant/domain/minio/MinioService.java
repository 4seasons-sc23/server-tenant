package com.instream.tenant.domain.minio;

import io.minio.*;
import io.minio.errors.MinioException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
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

    public Mono<Void> deleteFolder(String folderName) {
        return Mono.fromCallable(() -> {
                    // 폴더 내의 모든 객체 나열
                    Iterable<Result<Item>> objects = minioClient.listObjects(
                            ListObjectsArgs.builder().bucket(bucketName).prefix(folderName + "/").recursive(true).build());

                    // 삭제할 객체 목록 생성
                    List<DeleteObject> objectsToDelete = new ArrayList<>();
                    for (Result<Item> itemResult : objects) {
                        Item item = itemResult.get();
                        objectsToDelete.add(new DeleteObject(item.objectName()));
                    }

                    // 객체 삭제
                    if (!objectsToDelete.isEmpty()) {
                        minioClient.removeObjects(
                                RemoveObjectsArgs.builder()
                                        .bucket(bucketName)
                                        .objects(objectsToDelete)
                                        .build()
                        );
                    }
                    return null;
                }).subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}