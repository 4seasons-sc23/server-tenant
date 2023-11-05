package com.instream.tenant.domain.application.infra.converter.applicationType;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.infra.enums.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ApplicationTypeReadConverter implements Converter<String, ApplicationType> {
    @Override
    public ApplicationType convert(String source) {
        return ApplicationType.fromCode(source);
    }
}
