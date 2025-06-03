package com.walrex.module_common.flyway.manager;

import com.walrex.module_common.config.FlywayModuleProperties;
import com.walrex.module_common.flyway.api.FlywayModuleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlywayModuleManager {
    private final List<FlywayModuleConfig> moduleConfigs;
    private final FlywayModuleProperties properties;
    private final Environment environment;

    public void migrateAllModules() {
        if (!properties.isEnabled()) {
            log.info("Migraciones de módulos Flyway deshabilitadas. Saltando migraciones.");
            return;
        }

        log.info("Iniciando migraciones Flyway para todos los módulos");

        // Obtener credenciales de la base de datos
        String url = properties.getUrl() != null ? properties.getUrl() :
                environment.getProperty("spring.datasource.url",
                        environment.getProperty("spring.flyway.url"));

        String username = properties.getUsername() != null ? properties.getUsername() :
                environment.getProperty("spring.datasource.username",
                        environment.getProperty("spring.flyway.user"));

        String password = properties.getPassword() != null ? properties.getPassword() :
                environment.getProperty("spring.datasource.password",
                        environment.getProperty("spring.flyway.password"));

        if (url == null || username == null || password == null) {
            throw new IllegalStateException(
                    "No se pudo determinar la configuración de base de datos para migraciones Flyway. " +
                            "Por favor, configura spring.datasource.url o flyway.modules.url"
            );
        }

        // Ordenar módulos según prioridad
        List<FlywayModuleConfig> orderedConfigs = new ArrayList<>(moduleConfigs);
        orderedConfigs.sort(Comparator.comparingInt(FlywayModuleConfig::getOrder));

        // Ejecutar migraciones para cada módulo
        for (FlywayModuleConfig config : orderedConfigs) {
            migrateModule(config, url, username, password);
        }

        log.info("Migraciones Flyway completadas para todos los módulos");
    }

    /**
     * Ejecuta las migraciones Flyway para un módulo específico.
     */
    private void migrateModule(FlywayModuleConfig config, String url, String username, String password) {
        log.info("Migrando módulo: {} (esquema: {}, orden: {})",
                config.getName(), config.getSchema(), config.getOrder());

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .schemas(config.getSchema())
                .createSchemas(config.getCreateSchema())
                .locations(config.getLocations())
                .baselineOnMigrate(config.getBaselineOnMigrate())
                .load();

        if (properties.isVerbose()) {
            // Mostrar información sobre las migraciones pendientes
            try {
                log.info("Validando migraciones pendientes para módulo: {}", config.getName());
                flyway.validate();
            } catch (Exception e) {
                log.warn("Validación de migraciones falló para módulo: {}. Razón: {}",
                        config.getName(), e.getMessage());
            }
        }

        // Ejecutar la migración
        MigrateResult migrations = flyway.migrate();
        log.info("Módulo {} migrado exitosamente. Migraciones aplicadas: {}",
                config.getName(), migrations.migrationsExecuted);
    }
}
