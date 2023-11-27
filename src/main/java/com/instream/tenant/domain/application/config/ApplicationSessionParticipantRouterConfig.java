package com.instream.tenant.domain.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.request.ApplicationSessionSearchPaginationOptionRequest;
import com.instream.tenant.domain.application.infra.enums.ApplicationErrorCode;
import com.instream.tenant.domain.application.infra.enums.ApplicationSessionErrorCode;
import com.instream.tenant.domain.common.config.RouterConfig;
import com.instream.tenant.domain.common.infra.model.InstreamHttpHeaders;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.request.ParticipantJoinSearchPaginationOptionRequest;
import com.instream.tenant.domain.participant.handler.ParticipantHandler;
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
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Configuration
public class ApplicationSessionParticipantRouterConfig extends RouterConfig {
    private final String v1ApplicationSessionsRoutesTag = "v1-application-session-participant-routes";

    @Autowired
    public ApplicationSessionParticipantRouterConfig(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Bean
    public RouterFunction<ServerResponse> v1ApplicationSessionRoutes(ParticipantHandler participantHandler) {
        return route().nest(RequestPredicates.path("/v1/applications/sessions/{sessionId}/participants"),
                builder -> {
                    builder.add(searchParticipants(participantHandler));
                    builder.add(enter(participantHandler));
                    builder.add(leave(participantHandler));
                },
                ops -> ops.operationId("v1ApplicationRoutes")
                        .tag(v1ApplicationSessionsRoutesTag)
        ).build();
    }

    private RouterFunction<ServerResponse> searchParticipants(ParticipantHandler participantHandler) {
        return route()
                .GET(
                        "",
                        participantHandler::searchParticipantJoinWithSession,
                        this::buildSearchParticipantsSwagger
                )
                .build();
    }

    private RouterFunction<ServerResponse> enter(ParticipantHandler participantHandler) {
        return route().POST("/enter",
                participantHandler::enterToApplication,
                this::buildEnterParticipantSwagger
        ).build();
    }

    private RouterFunction<ServerResponse> leave(ParticipantHandler participantHandler) {
        return route().POST(
                        "/leave",
                        participantHandler::leaveFromApplication,
                        this::buildEndApplicationSessionSwagger
                )
                .build();
    }

    private void buildSearchParticipantsSwagger(Builder ops) {
        List<HttpErrorCode> httpErrorCodeList = new ArrayList<>(Arrays.asList(
                CommonHttpErrorCode.UNAUTHORIZED,
                CommonHttpErrorCode.BAD_REQUEST,
                CommonHttpErrorCode.INTERNAL_SERVER_ERROR
        ));

        buildHttpErrorResponse(ops, httpErrorCodeList);

        ops.operationId(String.format("pagination_%s", ParticipantJoinDto.class.getSimpleName()))
                .summary("해당 어플리케이션 세션의 참가자 세션 검색 API")
                .description("""
                        해당 어플리케이션 세션의 참가자 세션을 검색합니다.
                                                
                        정렬은 sort에서 원하는 옵션과 ASC(오름차순), DESC(내림차순)을 입력하시면 됩니다. name 항목은 DB 컬럼 기준 CamelCase로 입력받고 있습니다. 자세한 항목은 ErdCloud 참고해주세요.
                                                
                        QueryParameter에서는 sort[name]=createdAt&sort[option]=DESC 와 같은 방식으로 입력해주셔야 처리 가능합니다. Swagger에서 요청하면 Responses 항목에서 예시 url을 보실 수 있습니다.
                                                
                        전체 페이지 및 데이터 개수가 필요할 때만 firstView를 true로, 아니라면 false로 호출해주세요. SQL 성능 차이가 납니다.
                                                
                        현재 참가자 세션 기간별 조회 옵션을 지원합니다.
                                                
                        추가적인 옵션을 원할 경우 백엔드 팀한테 문의해주세요!
                        """)
                .tag(v1ApplicationSessionsRoutesTag)
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
                .parameter(parameterBuilder().in(ParameterIn.QUERY).name("option").implementation(ParticipantJoinSearchPaginationOptionRequest.class))
                .response(responseBuilder().implementation(ParticipantJoinDto.class));
    }

    private void buildEnterParticipantSwagger(Builder ops) {
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

        ops.operationId("enterParticipant")
                .summary("참가자의 어플리케이션 입장 API")
                .description("""
                        참가자가 어플리케이션에 입장합니다.
                        어플리케이션과 세션이 모두 활성화되어 있어야 합니다.
                        
                        입장 시 참가자의 닉네임과 프로필 이미지를 저장 또는 업데이트 합니다.
                        """)
                .tag(v1ApplicationSessionsRoutesTag)
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
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.CREATED.value()))
                        .content(contentBuilder().schema(schemaBuilder().implementation(ParticipantJoinDto.class)))
                );
    }

    private void buildEndApplicationSessionSwagger(Builder ops) {
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


        ops.operationId("leaveParticipant")
                .summary("참가자의 어플리케이션 퇴장 API")
                .description("""
                        참가자가 어플리케이션에 퇴장합니다.
                        어플리케이션과 세션이 모두 활성화되어 있어야 합니다.
                        """)
                .tag(v1ApplicationSessionsRoutesTag)
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
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.CREATED.value()))
                        .content(contentBuilder().schema(schemaBuilder().implementation(ParticipantJoinDto.class)))
                );
    }
}