package com.instream.tenant.core.config;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer customizePaginationDtoSchema() {
        return openApi -> {
            // DTO 클래스 목록
            List<Class<?>> dtoClasses = List.of(ApplicationDto.class);
            // 각 DTO 클래스에 대한 스키마 생성 및 등록
            dtoClasses.forEach(dtoClass -> {
                String schemaName = "PaginationDto" + dtoClass.getSimpleName();
                Schema<?> paginationDtoSchema = createPaginationDtoSchema(dtoClass);
                openApi.schema(schemaName, paginationDtoSchema);
                handleOperationResponseWithGenericSchema(openApi, String.format("pagination_%s", dtoClass.getSimpleName()), schemaName);
            });
        };
    }

    @Bean
    public OpenApiCustomizer customizeCollectionDtoSchema() {
        return openApi -> {
            // DTO 클래스 목록
            List<Class<?>> dtoClasses = List.of(ApplicationDto.class);
            // 각 DTO 클래스에 대한 스키마 생성 및 등록
            dtoClasses.forEach(dtoClass -> {
                String schemaName = "CollectionDto" + dtoClass.getSimpleName();
                Schema<?> collectionDtoSchema = createCollectionDtoSchema(dtoClass);
                openApi.schema(schemaName, collectionDtoSchema);
                handleOperationResponseWithGenericSchema(openApi, String.format("collection_%s", dtoClass.getSimpleName()), schemaName);
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

    private Schema<?> createPaginationDtoSchema(Class<?> clazz) {
        Schema<?> collectionDtoSchema = createCollectionDtoSchema(clazz);

        return (Schema<?>) new Schema<>()
                .addProperty("currentPage", new IntegerSchema().description("현재 페이지, 0부터 시작").minimum(BigDecimal.valueOf(0)))
                .addProperty("data", collectionDtoSchema)
                .addProperty("totalPages", new IntegerSchema().description("전체 페이지 수"))
                .addProperty("pageSize", new IntegerSchema().description("페이지 당 데이터 수"))
                .addProperty("totalElements", new IntegerSchema().description("전체 데이터 수"));
    }

    // CollectionDto<?> 스키마 생성
    private Schema<?> createCollectionDtoSchema(Class<?> clazz) {
        Schema<?> itemSchema = new Schema<>().$ref(clazz.getSimpleName());
        ArraySchema arraySchema = new ArraySchema().items(itemSchema);

        return new Schema<>()
                .addProperty("data", arraySchema)
                .addProperty("length", new IntegerSchema().description("데이터 개수"));
    }
}
