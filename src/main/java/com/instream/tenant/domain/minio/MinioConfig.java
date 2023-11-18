package com.instream.media.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    private String endpoint = "https://minio.aolda.net";
    private String accessKey = "ZYH8rcgdl0okiPtV4eHD";
    private String secretKey = "bksx22pdUW705EJPojbKQu9ZnfRpZ3v7PYXZ0Wjm";

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}
