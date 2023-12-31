package com.instream.tenant.core.config;

import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeReadConverter;
import com.instream.tenant.domain.application.infra.converter.applicationType.ApplicationTypeWriteConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDReadConverter;
import com.instream.tenant.domain.common.infra.converter.uuid.UUIDWriteConverter;
import com.instream.tenant.domain.serviceError.infra.converter.IsAnsweredReadConverter;
import com.instream.tenant.domain.serviceError.infra.converter.IsAnsweredWriteConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusReadConverter;
import com.instream.tenant.domain.tenant.infra.converter.StatusWriteConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {

    private final List<Converter<?, ?>> converters;

    @Autowired
    public R2dbcConfig(List<Converter<?, ?>> converters) {
        this.converters = converters;
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }
}
