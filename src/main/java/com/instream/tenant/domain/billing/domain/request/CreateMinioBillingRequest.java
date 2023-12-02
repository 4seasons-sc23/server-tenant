package com.instream.tenant.domain.billing.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class CreateMinioBillingRequest {
    @JsonProperty("EventName")
    @Schema(description = "Minio 이벤트 웹훅 이름", example = "s3:ObjectAccessed:Get")
    private String eventName;
    @JsonProperty("Key")
    @Schema(description = "Minio 내 파일 이름", example = "bucketName/itemPath/itemName.ts")
    private String key;
    @JsonProperty("Records")
    @Schema(description = "Minio 접근 기록")
    private List<Record> records;

    public static class Record {
        @JsonProperty("eventVersion")
        @Schema(example = "2.0")
        private String eventVersion;
        @JsonProperty("eventSource")
        @Schema(example = "minio:s3")
        private String eventSource;
        @JsonProperty("awsRegion")
        private String awsRegion;
        @JsonProperty("eventTime")
        @Schema(example = "2023-11-30T13:40:18.486Z")
        private String eventTime;
        @JsonProperty("eventName")
        @Schema(description = "이벤트 이름",example = "s3:ObjectAccessed:Get")
        private String eventName;
        @JsonProperty("userIdentity")
        private UserIdentity userIdentity;
        @JsonProperty("requestParameters")
        private RequestParameters requestParameters;
        @JsonProperty("responseElements")
        private ResponseElements responseElements;
        @JsonProperty("s3")
        private S3 s3;
        @JsonProperty("source")
        private Source source;
    }

    public static class UserIdentity {
        @JsonProperty("principalId")
        private String principalId;
    }

    public static class RequestParameters {
        @JsonProperty("principalId")
        private String principalId;
        @JsonProperty("region")
        private String region;
        @JsonProperty("sourceIPAddress")
        @Schema(example = "10.16.0.1")
        private String sourceIPAddress;
    }

    public static class ResponseElements {
        @JsonProperty("content-length")
        @Schema(description = "파일 크기(Byte)", example = "209056")
        private String contentLength;
        @JsonProperty("x-amz-id-2")
        @Schema(example = "dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8")
        private String xAmzId2;
        @JsonProperty("x-amz-request-id")
        @Schema(example = "179C6A4869982EEB")
        private String xAmzRequestId;
        @JsonProperty("x-minio-deployment-id")
        @Schema(example = "40b2e23d-0c91-4179-8786-6e5101a8a279")
        private String xMinioDeploymentId;
        @JsonProperty("x-minio-origin-endpoint")
        @Schema(example = "http://localhost:9000")
        private String xMinioOriginEndpoint;
    }

    public static class S3 {
        @JsonProperty("s3SchemaVersion")
        @Schema(example = "1.0")
        private String s3SchemaVersion;
        @JsonProperty("configurationId")
        @Schema(example = "Config")
        private String configurationId;
        @JsonProperty("bucket")
        private Bucket bucket;
        @JsonProperty("object")
        private ObjectEntity object;
    }

    public static class Bucket {
        @JsonProperty("name")
        @Schema(example = "bucketName")
        private String name;
        @JsonProperty("ownerIdentity")
        private OwnerIdentity ownerIdentity;
        @JsonProperty("arn")
        @Schema(example = "arn:aws:s3:::bucketName")
        private String arn;
    }

    public static class OwnerIdentity {
        @JsonProperty("principalId")
        private String principalId;
    }

    public static class ObjectEntity {
        @JsonProperty("key")
        @Schema(description = "파일 이름", example = "itemPath%2FitemName.ts")
        private String key;
        @JsonProperty("size")
        @Schema(description = "파일 크기(Byte 기준)", example = "209056")
        private Long size;
        @JsonProperty("eTag")
        @Schema(example = "27ffd2113b5ae57632d529759d5589a8")
        private String eTag;
        @JsonProperty("contentType")
        @Schema(example = "video/mp2t")
        private String contentType;
        @JsonProperty("userMetadata")
        private UserMetadata userMetadata;
        @JsonProperty("sequencer")
        @Schema(example = "1798191ADAD8BEFC")
        private String sequencer;
    }

    public static class UserMetadata {
        @JsonProperty("content-type")
        @Schema(example = "video/mp2t")
        private String contentType;
    }

    public static class Source {
        @JsonProperty("host")
        @Schema(example = "10.16.0.1")
        private String host;
        @JsonProperty("port")
        @Schema(example = "")
        private String port;
        @JsonProperty("userAgent")
        @Schema(example = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
        private String userAgent;
    }
}
