package com.instream.tenant.domain.serviceError.infra.converter;

import com.instream.tenant.domain.serviceError.infra.enums.IsAnswered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class IsAnsweredReadConverter implements Converter<String, IsAnswered> {
    @Override
    public IsAnswered convert(String source) {
        return IsAnswered.fromCode(source);
    }
}