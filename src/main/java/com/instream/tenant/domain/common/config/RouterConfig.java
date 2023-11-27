package com.instream.tenant.domain.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.common.infra.enums.SwaggerErrorResponseBuilderTemplate;
import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class RouterConfig {
    protected final ObjectMapper objectMapper;

    public RouterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void buildHttpErrorResponse(Builder ops, List<HttpErrorCode> httpErrorCodeList) {
        httpErrorCodeList.forEach(httpErrorCode -> ops.response(SwaggerErrorResponseBuilderTemplate.basic.getTemplateFunction().apply(httpErrorCode, objectMapper)));
    }
}
