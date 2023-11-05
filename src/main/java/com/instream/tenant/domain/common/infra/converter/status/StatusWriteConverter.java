package com.instream.tenant.domain.common.infra.converter.status;

import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class StatusWriteConverter implements Converter<Status, String> {
    @Override
    public String convert(Status source) {
        return source.getCode();
    }
}

