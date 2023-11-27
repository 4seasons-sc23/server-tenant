package com.instream.tenant.domain.tenant.infra.converter;

import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@WritingConverter
public class StatusWriteConverter implements Converter<Status, String> {
    @Override
    public String convert(Status source) {
        return source.getCode();
    }
}

