package com.instream.tenant.domain.application.infra.converter.applicationType;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ApplicationTypeWriteConverter implements Converter<ApplicationType, String> {
    @Override
    public String convert(ApplicationType source) {
        return source.getCode();
    }
}

