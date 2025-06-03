package com.walrex.module_articulos.config.r2dbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages =
    "com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.repository"
)
public class ArticuloR2dbcConfig {

}
