package com.instream.tenant.security.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.security.filter.ApplicationApiKeyAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {
    @Bean
    public ApplicationApiKeyAuthFilter applicationApiKeyAuthFilter(ApplicationRepository applicationRepository, ObjectMapper objectMapper) {
        return new ApplicationApiKeyAuthFilter(applicationRepository, objectMapper);
    }
}
