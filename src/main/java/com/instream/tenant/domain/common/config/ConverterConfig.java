package com.instream.tenant.domain.common.config;

import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeReadConverter;
import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeWriteConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDReadConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDWriteConverter;
import com.instream.tenant.domain.serviceError.infra.converter.IsAnsweredReadConverter;
import com.instream.tenant.domain.serviceError.infra.converter.IsAnsweredWriteConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusReadConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Configuration
public class ConverterConfig {

    @Bean
    public List<Converter<?, ?>> converters() {
        return List.of(
            new StatusWriteConverter(), new StatusReadConverter(),
            new UUIDReadConverter(), new UUIDWriteConverter(),
            new ApplicationTypeReadConverter(), new ApplicationTypeWriteConverter(),
            new IsAnsweredReadConverter(), new IsAnsweredWriteConverter()
        );
    }
}
