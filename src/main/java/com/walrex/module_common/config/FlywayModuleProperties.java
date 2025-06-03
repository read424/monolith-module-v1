package com.walrex.module_common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "flyway.modules")
@Data
public class FlywayModuleProperties {
    /**
     * Si las migraciones modulares están habilitadas.
     */
    private boolean enabled = true;

    /**
     * URL de la base de datos para migraciones. Si no se especifica, se utilizará spring.datasource.url
     */
    private String url;

    /**
     * Usuario de la base de datos. Si no se especifica, se utilizará spring.datasource.username
     */
    private String username;

    /**
     * Contraseña de la base de datos. Si no se especifica, se utilizará spring.datasource.password
     */
    private String password;

    /**
     * Si se debe mostrar información detallada durante las migraciones.
     */
    private boolean verbose = false;
}
