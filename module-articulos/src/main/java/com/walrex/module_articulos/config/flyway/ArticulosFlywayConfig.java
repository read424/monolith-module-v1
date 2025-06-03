package com.walrex.module_articulos.config.flyway;

import com.walrex.module_common.flyway.annotation.EnableFlywayModule;
import com.walrex.module_common.flyway.api.FlywayModuleConfig;
import org.springframework.stereotype.Component;

@Component
@EnableFlywayModule
public class ArticulosFlywayConfig  implements FlywayModuleConfig {
    @Override
    public String getName() {
        return "articulos";
    }

    @Override
    public String getSchema() {
        return "logistica";
    }

    @Override
    public String[] getLocations() {
        return new String[]{"classpath:db/migration/"};
    }

    @Override
    public int getOrder() {
        return 10; // Prioridad
    }
}
