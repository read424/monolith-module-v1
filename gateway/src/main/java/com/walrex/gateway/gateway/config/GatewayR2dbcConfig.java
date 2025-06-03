package com.walrex.gateway.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages =
        "com.walrex.gateway.gateway"
)
public class GatewayR2dbcConfig {
}
