package com.instream.tenant.domain.media.config;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.externaldocumentation.Builder.externalDocumentationBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.request.NginxRtmpStreamEvent;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.media.domain.request.MediaUploadRequest;
import com.instream.tenant.domain.media.handler.MediaHandler;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class MediaRouterConfig extends RouterConfig {
    private final String v1MediaRoutesTag = "v1-media-routes";

    @Autowired
    public MediaRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1MediaRoutes(MediaHandler mediaHandler) {
        return route().nest(RequestPredicates.path("/v1/medias"),
                builder -> {
                    builder.add(startNginxRtmpStream(mediaHandler));
                    builder.add(endNginxRtmpStream(mediaHandler));
                    builder.add(uploadHlsFiles(mediaHandler));
                },
                ops -> ops.operationId("v1-media-routes")
                        .tag(v1MediaRoutesTag)
        ).build();
    }

    private RouterFunction<ServerResponse> startNginxRtmpStream(MediaHandler mediaHandler) {
        return route().POST(
                        "/start",
                        mediaHandler::startNginxRtmpStream,
                        this::buildStartNginxRtmpStreamSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> endNginxRtmpStream(MediaHandler mediaHandler) {
        return route().POST(
                        "/end",
                        mediaHandler::endNginxRtmpStream,
                        this::buildEndNginxRtmpStreamSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> uploadHlsFiles(MediaHandler mediaHandler) {
        return route().POST(
                        "/upload/hls/{quality}",
                        mediaHandler::uploadMedia,
                        this::buildUploadHlsFilesSwagger
                )
                .build();
    }

    private void buildStartNginxRtmpStreamSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_SUPPORTED,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("startNginxRtmpStreamSwagger")
                .summary("Nginx RTMP 기반 어플리케이션 세션 시작")
                .description("""
                        Request Body은 Nginx RTMP 명세에 따라 작성하였습니다.
                                                
                        call=connect
                                                
                        addr - client IP address
                                                
                        app - application name
                                                
                        flashVer - client flash version
                                                
                        swfUrl - client swf url
                                                
                        tcUrl - tcUrl
                                                
                        pageUrl - client page url 
                                                
                        name - stream name (이부분이 API KEY)
                        """)
                .externalDocs(externalDocumentationBuilder().url("https://github.com/arut/nginx-rtmp-module/wiki/Directives#on_play").description("Nginx RTMP 명세; Request Body 내용"))
                .externalDocs(externalDocumentationBuilder().url("https://github.com/arut/nginx-rtmp-module/wiki/Directives#on_publish").description("Nginx RTMP 명세; 실제 사용하는 옵션"))
                .tag(v1MediaRoutesTag)
                .parameter(parameterBuilder()
                        .name(InstreamHttpHeaders.API_KEY)
                        .description("API Key")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.CREATED.value())).implementation(ApplicationSessionDto.class));

    }

    private void buildEndNginxRtmpStreamSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_SUPPORTED,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("endNginxRtmpStreamSwagger")
                .summary("Nginx RTMP 기반 어플리케이션 세션 종료")
                .description("""
                        Request Body은 Nginx RTMP 명세에 따라 작성하였습니다.
                                                
                        call=connect
                                                
                        addr - client IP address
                                                
                        app - application name
                                                
                        flashVer - client flash version
                                                
                        swfUrl - client swf url
                                                
                        tcUrl - tcUrl
                                                
                        pageUrl - client page url
                                                
                        name - stream name (이부분이 API KEY)
                        """)
                .externalDocs(externalDocumentationBuilder().url("https://github.com/arut/nginx-rtmp-module/wiki/Directives#on_play").description("Nginx RTMP 명세; Request Body 내용"))
                .externalDocs(externalDocumentationBuilder().url("https://github.com/arut/nginx-rtmp-module/wiki/Directives#on_publish").description("Nginx RTMP 명세; 실제 사용하는 옵션"))
                .tag(v1MediaRoutesTag)
                .parameter(parameterBuilder()
                        .name(InstreamHttpHeaders.API_KEY)
                        .description("API Key")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .requestBody(requestBodyBuilder().implementation(NginxRtmpStreamEvent.class).required(true).description("Nginx publishing request"))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value())).implementation(ApplicationSessionDto.class));
    }

    private void buildUploadHlsFilesSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_SUPPORTED,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId("uploadHlsFiles")
                .summary("HLS File들을 업로드 합니다.")
                .description("""
                        HLS File들을 업로드 합니다.
                                                
                        m3u8 파일의 Content-type은 application/vnd.apple.mpegurl
                                                
                        ts 파일의 Content-type은 video/MP2T
                                                
                        이렇게 설정해주셔야 합니다.
                        """)
                .tag(v1MediaRoutesTag)
                .parameter(parameterBuilder()
                        .name(InstreamHttpHeaders.API_KEY)
                        .description("API Key")
                        .in(ParameterIn.HEADER)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .parameter(parameterBuilder()
                        .name("quality")
                        .description("현재 영상의 퀄리티입니다. 360 / 720 / 1080")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("360"))
                .requestBody(requestBodyBuilder().implementation(MediaUploadRequest.class).required(true).description("Hls Files"))
                .response(responseBuilder().responseCode("200"));
    }

}
