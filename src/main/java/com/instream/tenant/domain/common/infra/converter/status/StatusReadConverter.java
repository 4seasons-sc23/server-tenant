package com.instream.tenant.domain.common.infra.converter.status;

import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StatusReadConverter implements Converter<String, Status> {
    @Override
    public Status convert(String source) {
        return Status.fromCode(source);
    }
}
