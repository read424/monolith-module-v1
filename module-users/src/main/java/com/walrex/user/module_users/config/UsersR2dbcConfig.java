package com.walrex.user.module_users.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages =
        "com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository"
)
public class UsersR2dbcConfig {
}
