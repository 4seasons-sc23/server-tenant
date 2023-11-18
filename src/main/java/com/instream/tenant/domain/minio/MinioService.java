package com.instream.media.minio;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    //    @Value("${minio.bucket}")
    private String bucketName = "instream";

    public void test()
        throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
        NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
        XmlParserException, InternalException {
        boolean isBucketExist = minioClient
            .bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        System.out.println("isBucketExist: " + isBucketExist);
    }

    public void uploadToMinio(String filePath, String objectPathh) throws IOException {
        System.out.println("=========== uploadToMinio ===========");
        try {

            // 파일 업로드
            try (InputStream is = new FileInputStream(filePath)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectPathh)
                        .stream(is, new File(filePath).length(), -1)
                        .contentType("video/mpeg")
                        .build());
            }
            System.out.println("========== 업로드 완료 =========== " + filePath);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.err.println("Error occurred: " + e);
        }
    }
}