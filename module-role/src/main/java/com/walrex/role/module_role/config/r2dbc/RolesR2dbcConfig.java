package com.walrex.role.module_role.config.r2dbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages =
        "com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.repository"
)
public class RolesR2dbcConfig {
}
