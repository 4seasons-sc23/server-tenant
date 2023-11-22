package com.instream.tenant.domain.tenant.infra.converter;

import com.instream.tenant.domain.common.infra.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@ReadingConverter
@Slf4j
public class StatusReadConverter implements Converter<String, Status> {
    @Override
    public Status convert(String source) {
        log.info("hello {}", source);
        return Status.fromCode(source);
    }
}
