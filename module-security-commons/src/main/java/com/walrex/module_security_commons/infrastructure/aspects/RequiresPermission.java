package com.walrex.module_security_commons.infrastructure.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar que el usuario tenga un permiso específico
 *
 * Uso en handlers:
 * <pre>
 * {@code
 * @RequiresPermission("almacen:kardex:read")
 * public Mono<ServerResponse> getKardex(ServerRequest request) {
 *     // El aspecto valida automáticamente antes de ejecutar
 *     return ...;
 * }
 * }
 * </pre>
 *
 * @author Security Commons Module
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    String value();

    String message() default "No tienes permiso apra realizar esta acción";
}
