package com.instream.tenant.core.config;

import com.instream.tenant.domain.host.infra.converter.StatusReadConverter;
import com.instream.tenant.domain.host.infra.converter.StatusWriteConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.List;

@Configuration
public class R2dbcConfig {
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = List.of(new StatusWriteConverter(), new StatusReadConverter());
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }
}
