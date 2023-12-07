package com.instream.tenant.domain.media.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.instream.tenant.domain.application.service.ApplicationService;
import com.instream.tenant.domain.common.infra.helper.HandlerHelper;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequest;
import com.instream.tenant.domain.media.domain.request.NginxRtmpRequest;
import com.instream.tenant.domain.media.service.MediaService;
import com.instream.tenant.domain.minio.MinioService;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MediaHandler {
    private final MediaService mediaService;

    private final ApplicationService applicationService;

    public MediaHandler(MediaService mediaService, ApplicationService applicationService) {
        this.mediaService = mediaService;
        this.applicationService = applicationService;
    }

    public Mono<ServerResponse> getRecentSession(ServerRequest request) {
        String apiKey = request.headers().firstHeader(InstreamHttpHeaders.API_KEY);
        Mono<UUID> applicationIdMono = HandlerHelper.getUUIDFromPathVariable(request, "applicationId");
        return applicationIdMono
                .flatMap(applicationId -> applicationService.getRecentSession(apiKey, applicationId))
                .flatMap(applicationSessionDto -> ServerResponse.ok().bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> startNginxRtmpStream(ServerRequest request) {
        request.queryParams().forEach((key, value) -> log.info("startNginxRtmpStream request {} {}", key, value));
        return NginxRtmpRequest.fromQueryParams(request.queryParams())
                .flatMap(nginxRtmpRequest -> {
                    try {
                        UUID applicationId = UUID.fromString(Objects.requireNonNull(getParameter(nginxRtmpRequest.tcurl(), "id")));
                        return applicationService.startApplicationSession(applicationId, nginxRtmpRequest.name());
                    } catch (Exception e) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                    }
                })
                .flatMap(applicationSessionDto -> ServerResponse.created(URI.create("")).bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> endNginxRtmpStream(ServerRequest request) {
        request.queryParams().forEach((key, value) -> log.info("endNginxRtmpStream request {} {}", key, value));
        return NginxRtmpRequest.fromQueryParams(request.queryParams())
                .flatMap(nginxRtmpRequest -> {
                    try {
                        UUID applicationId = UUID.fromString(Objects.requireNonNull(getParameter(nginxRtmpRequest.tcurl(), "id")));
                        return applicationService.endApplicationSession(applicationId, nginxRtmpRequest.name());
                    } catch (Exception e) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                    }
                })
                .flatMap(applicationSessionDto -> ServerResponse.ok().bodyValue(applicationSessionDto));
    }

    public Mono<ServerResponse> uploadMedia(ServerRequest request) {
        Mono<UUID> sessionIdMono = HandlerHelper.getUUIDFromPathVariable(request, "sessionId");
        int quality;

        try {
            quality = Integer.parseInt(request.pathVariable("quality"));
        } catch (IllegalArgumentException e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }

        return sessionIdMono.flatMap(sessionId -> request.body(BodyExtractors.toMultipartData())
                .flatMap(stringPartMultiValueMap -> {
                    Map<String, Part> partMap = stringPartMultiValueMap.toSingleValueMap();
                    Part ts = partMap.get("ts");

                    if (ts == null) {
                        return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
                    }

                    return Mono.just(MediaUploadRequest.builder()
                            .m3u8Main((FilePart) partMap.get("m3u8Main"))
                            .m3u8((FilePart) partMap.get("m3u8"))
                            .ts((FilePart) ts)
                            .quality(quality)
                            .build());
                })
                .flatMap(uploadRequest -> mediaService.uploadMedia(uploadRequest, sessionId))
                .flatMap(result -> ok().bodyValue(result)));
    }

    private String getParameter(String url, String parameterName) {
        int paramStart = url.indexOf("?" + parameterName + "=");
        if (paramStart == -1) {
            paramStart = url.indexOf("&" + parameterName + "=");
            if (paramStart == -1) {
                return null; // 파라미터가 존재하지 않음
            }
        }

        int start = paramStart + parameterName.length() + 2; // 파라미터 이름과 '=' 문자를 넘어감
        int end = url.indexOf('&', start);
        if (end == -1) {
            end = url.length();
        }

        return url.substring(start, end);
    }
}
