package com.instream.tenant.domain.common.infra.enums;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import lombok.Getter;
import org.springdoc.core.fn.builders.apiresponse.Builder;

import java.util.function.BiFunction;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;

@Getter
public enum SwaggerErrorResponseBuilderTemplate {
    basic((httpErrorCode, objectMapper) -> {
        try {
            return responseBuilder()
                    .responseCode(String.valueOf(httpErrorCode.getHttpStatus().value()))
                    .content(contentBuilder().example(exampleOjectBuilder().name(httpErrorCode.getCode()).value(objectMapper.writeValueAsString(httpErrorCode))));
        } catch (JsonProcessingException e) {
            return responseBuilder().responseCode(httpErrorCode.getCode());
        }
    });

    private final BiFunction<HttpErrorCode, ObjectMapper, Builder> templateFunction;

    SwaggerErrorResponseBuilderTemplate(BiFunction<HttpErrorCode, ObjectMapper, Builder> templateFunction) {
        this.templateFunction = templateFunction;
    }
}
