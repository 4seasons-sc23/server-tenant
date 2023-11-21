package com.instream.tenant.core.config;

import com.instream.tenant.domain.application.repository.ApplicationRepository;
import com.instream.tenant.security.filter.ApplicationApiKeyAuthFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final ApplicationApiKeyAuthFilter applicationApiKeyAuthFilter;

    public SecurityConfig(ApplicationApiKeyAuthFilter applicationApiKeyAuthFilter) {
        this.applicationApiKeyAuthFilter = applicationApiKeyAuthFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/protected/**").authenticated()
                        .anyExchange().permitAll()
                )
                .addFilterAt(applicationApiKeyAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf().disable()
                .cors().disable();

        return http.build();
    }
}
