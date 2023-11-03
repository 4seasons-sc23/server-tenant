package com.instream.tenant.core.config;

import com.google.common.base.CaseFormat;
import com.infobip.spring.data.jdbc.annotation.processor.ProjectColumnCaseFormat;
import com.infobip.spring.data.jdbc.annotation.processor.ProjectTableCaseFormat;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.relational.core.mapping.NamingStrategy;

@Configuration
@ProjectTableCaseFormat(CaseFormat.LOWER_UNDERSCORE)
@ProjectColumnCaseFormat(CaseFormat.LOWER_UNDERSCORE)
public class QuerydslConfiguration {

    @Bean
    public SQLTemplates mariaDbTemplates() {
        return new MySQLTemplates();
    }

    @Bean
    @Primary
    public NamingStrategy namingStrategy() {
        return new NamingStrategy() {};
    }
}
