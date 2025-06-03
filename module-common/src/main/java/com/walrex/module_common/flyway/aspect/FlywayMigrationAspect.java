package com.walrex.module_common.flyway.aspect;

import com.walrex.module_common.flyway.manager.FlywayModuleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0) // Alta prioridad
@Slf4j
@RequiredArgsConstructor
public class FlywayMigrationAspect {
    private final FlywayModuleManager flywayModuleManager;

    /**
     * Ejecuta las migraciones Flyway antes de que se inicialice completamente la aplicación.
     */
    @Before("@within(org.springframework.boot.autoconfigure.SpringBootApplication)")
    public void executeBeforeApplicationStart() {
        log.info("Interceptando inicio de aplicación para migraciones Flyway");
        flywayModuleManager.migrateAllModules();
    }
}
