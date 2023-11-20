package com.instream.tenant.domain.minio;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
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

    public Mono<String> uploadFile(String objectName, File file, String contentType) {
        log.debug("objectName: {}, contentType: {}", objectName, contentType);

        if (file == null) {
            log.warn("file is null objectName: {}, contentType: {}", objectName, contentType);
            return Mono.just(String.format("objectName: %s, contentType: %s", objectName, contentType));
        }

        return Mono.fromCallable(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                log.debug("putObject {} {} {}", bucketName, objectName, contentType);

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(fileInputStream, file.length(), 10485760)
                                .contentType(contentType)
                                .build());

                return "File uploaded successfully !!";
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("Error uploading file to MinIO: ", e);
                throw new RuntimeException("Error uploading file to MinIO", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}