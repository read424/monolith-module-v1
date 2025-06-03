package com.walrex.module_common.flyway.api;

public interface FlywayModuleConfig {
    /**
     * @return El nombre único del módulo
     */
    String getName();

    /**
     * @return El esquema de base de datos donde se aplicarán las migraciones
     */
    String getSchema();

    /**
     * @return Las ubicaciones de los scripts de migración (formato classpath:path)
     */
    String[] getLocations();

    /**
     * @return Orden de ejecución (menor número = ejecución más temprana)
     */
    int getOrder();

    /**
     * @return Si se debe crear el esquema automáticamente si no existe
     */
    default boolean getCreateSchema() {
        return true;
    }

    /**
     * @return Si se debe realizar baseline al migrar
     */
    default boolean getBaselineOnMigrate() {
        return true;
    }
}
