package com.instream.tenant.domain.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.chat.domain.request.SendChatRequest;
import com.instream.tenant.domain.chat.handler.ChatHandler;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.common.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
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

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class ChatRouterConfig extends RouterConfig {
    private final String v1ChatRoutesTag = "v1-chat-routes";

    @Autowired
    public ChatRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1ChatRoutes(ChatHandler chatHandler) {
        return route().nest(RequestPredicates.path("/v1/chats/{sessionId}"),
                builder -> {
                    builder.add(sendChat(chatHandler));
                },
                ops -> ops.operationId("v1ChatRoutes")
                        .tag(v1ChatRoutesTag)
        ).build();
    }

    private RouterFunction<ServerResponse> sendChat(ChatHandler chatHandler) {
        return route().POST(
                        "/send",
                        chatHandler::sendChat,
                        this::buildSendChatSwagger
                )
                .build();
    }

    private void buildSendChatSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                ApplicationErrorCode.APPLICATION_NOT_SUPPORTED,
                ApplicationErrorCode.APPLICATION_NOT_FOUND,
                ApplicationSessionErrorCode.APPLICATION_SESSION_ALREADY_ENDED,
                ApplicationSessionErrorCode.APPLICATION_SESSION_NOT_FOUND,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);


        ops.operationId("sendChat")
                .summary("채팅 보내기 API")
                .description("""
                        참가자가 채팅을 보냅니다.
                        """)
                .tag(v1ChatRoutesTag)
                .parameter(parameterBuilder()
                        .name(InstreamHttpHeaders.API_KEY)
                        .description("API Key")
                        .in(ParameterIn.HEADER)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .parameter(parameterBuilder()
                        .name("sessionId")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .example("80bd6328-76a7-11ee-b720-0242ac130003"))
                .requestBody(requestBodyBuilder().implementation(SendChatRequest.class))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.CREATED.value()))
                        .content(contentBuilder().schema(schemaBuilder().implementation(SendChatRequest.class)))
                );
    }
}
