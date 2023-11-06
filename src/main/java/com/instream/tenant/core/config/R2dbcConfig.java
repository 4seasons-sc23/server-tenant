package com.instream.tenant.core.config;

import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeReadConverter;
import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeWriteConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDReadConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDWriteConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusReadConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {
    private final List<Converter<?, ?>> converters = List.of(
            new StatusWriteConverter(), new StatusReadConverter(),
            new UUIDReadConverter(), new UUIDWriteConverter(),
            new ApplicationTypeReadConverter(), new ApplicationTypeWriteConverter()
    );

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }
}
