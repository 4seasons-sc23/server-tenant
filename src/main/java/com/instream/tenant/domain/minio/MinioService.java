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

    public Mono<String> uploadFile(String objectName, FilePart filePart, String contentType) {
        if(filePart == null) {
            log.warn(String.format("objectName: %s, filePart: %s, contentType: %s"));
            return Mono.just(String.format("objectName: %s, filePart: %s, contentType: %s"));
        }

        return Mono.fromCallable(() -> {
            try {
                // DataBuffer Flux를 InputStream으로 변환
                InputStream inputStream = DataBufferUtils
                    .join(filePart.content())
                    .map(dataBuffer -> dataBuffer.asInputStream(true))
                    .block();

                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, -1, 10485760)
                        .contentType(contentType)
                        .build());
            } catch (MinioException e) {
                throw new RuntimeException("Error uploading file to MinIO", e);
            }

            return "File uploaded successfully !!";
        }).subscribeOn(Schedulers.boundedElastic());
    }
}