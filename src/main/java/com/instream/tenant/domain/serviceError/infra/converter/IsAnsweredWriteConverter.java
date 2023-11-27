package com.instream.tenant.domain.serviceError.infra.converter;

import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;


@WritingConverter
public class IsAnsweredWriteConverter implements Converter<IsAnswered, String> {
    @Override
    public String convert(IsAnswered source) {
        return source.getCode();
    }
}