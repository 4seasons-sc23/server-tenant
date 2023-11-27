package com.instream.tenant.core.config;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    /**
     * CollectionDto 또는 PaginationDto로 Wrapping 된 Dto Class를 Swagger에 보여주기 위한 목록입니다.
     *
     * RouterHandler에서 CollectionDto 또는 PaginationDto를 return 할 때, RouterConfig에서 Dto 클래스의 메타 데이터를 등록해줘야 합니다.
     * ```java
     * return route()
     *  .GET(
     *      "",
     *      applicationHandler::searchApplication,
     *      // operationId는 pagination_ 또는 collection_ 이라는 prefix가 있어야 합니다.
     *      ops -> ops.operationId(String.format("pagination_%s", ApplicationWithApiKeyDto.class.getSimpleName()))
     *          // 이렇게 등록해야 Dto class를 인식
     *          .response(responseBuilder().implementation(ApplicationWithApiKeyDto.class))
     * )
     * .build();
     * ```
     */
    private final List<Class<?>> customizeDtoSchemaList = List.of(
            ApplicationWithApiKeyDto.class, ApplicationSessionDto.class, ParticipantJoinDto.class
    );

    @Bean
    public OpenApiCustomizer customizeCollectionDtoSchema() {
        return openApi -> {
            // 각 DTO 클래스에 대한 스키마 생성 및 등록
            customizeDtoSchemaList.forEach(dtoClass -> {
                String schemaName = "CollectionDto" + dtoClass.getSimpleName();
                Schema<?> collectionDtoSchema = createCollectionDtoSchema(dtoClass);
                openApi.schema(schemaName, collectionDtoSchema);
                handleOperationResponseWithGenericSchema(openApi, String.format("collection_%s", dtoClass.getSimpleName()), schemaName);
            });
        };
    }

    @Bean
    public OpenApiCustomizer customizePaginationDtoSchema() {
        return openApi -> {
            // 각 DTO 클래스에 대한 스키마 생성 및 등록
            customizeDtoSchemaList.forEach(dtoClass -> {
                String schemaName = "PaginationDto" + dtoClass.getSimpleName();
                Schema<?> paginationDtoSchema = createPaginationDtoSchema(dtoClass);
                openApi.schema(schemaName, paginationDtoSchema);
                handleOperationResponseWithGenericSchema(openApi, String.format("pagination_%s", dtoClass.getSimpleName()), schemaName);
            });
        };
    }

    private void handleOperationResponseWithGenericSchema(OpenAPI openApi, String operationId, String schemaName) {
        openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> {
                    if (operation.getOperationId().equals(operationId)) {
                        ApiResponse apiResponse = new ApiResponse()
                                .description("OK")
                                .content(new Content()
                                        .addMediaType(
                                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                new MediaType().schema(new Schema<>().$ref(schemaName))
                                        )
                                );
                        operation.responses(new ApiResponses()._default(apiResponse));
                    }
                });
    }

    // CollectionDto<?> 스키마 생성
    private Schema<?> createCollectionDtoSchema(Class<?> clazz) {
        Schema<?> itemSchema = new Schema<>().$ref(clazz.getSimpleName());
        ArraySchema arraySchema = new ArraySchema().items(itemSchema);

        return new Schema<>()
                .addProperty("data", arraySchema)
                .addProperty("length", new IntegerSchema().description("데이터 개수"));
    }

    private Schema<?> createPaginationDtoSchema(Class<?> clazz) {
        Schema<?> collectionDtoSchema = createCollectionDtoSchema(clazz);

        return (Schema<?>) new Schema<>()
                .addProperty("currentPage", new IntegerSchema().description("현재 페이지, 0부터 시작").minimum(BigDecimal.valueOf(0)))
                .addProperty("data", collectionDtoSchema)
                .addProperty("totalPages", new IntegerSchema().description("전체 페이지 수").minimum(BigDecimal.valueOf(0)))
                .addProperty("pageSize", new IntegerSchema().description("페이지 당 데이터 수").minimum(BigDecimal.valueOf(0)))
                .addProperty("totalElements", new IntegerSchema().description("전체 데이터 수").minimum(BigDecimal.valueOf(0)));
    }

//    private Object createExampleFromClass(Class<?> clazz) {
//        try {
//            // 예제 객체를 저장할 Map
//            Map<String, Object> exampleValues = new HashMap<>();
//
//            // 클래스의 모든 필드에 대해 반복
//            for (Field field : clazz.getDeclaredFields()) {
//                io.swagger.v3.oas.annotations.media.Schema schemaAnnotation = field.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
//                if (schemaAnnotation != null) {
//                    // @Schema 어노테이션이 있으면, example 값을 가져옴
//                    String exampleValue = schemaAnnotation.example();
//                    if (!exampleValue.isEmpty()) {
//                        // 필드명과 예제 값으로 Map에 추가
//                        exampleValues.put(field.getName(), exampleValue);
//                    }
//                }
//            }
//
//            // Map을 JSON 객체로 변환 (여기서는 단순화를 위해 문자열로 표현)
//            return exampleValues.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
