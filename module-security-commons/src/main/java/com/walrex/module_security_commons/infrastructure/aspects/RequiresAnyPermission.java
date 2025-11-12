package com.walrex.module_security_commons.infrastructure.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para validar que el usuario tenga al menos uno de los permisos
 *
 * Uso en handlers:
 * <pre>
 * {@code
 * @RequiresAnyPermission({"almacen:kardex:read", "almacen:admin"})
 * public Mono<ServerResponse> getKardex(ServerRequest request) {
 *     // El aspecto valida que tenga al menos uno de los permisos
 *     return ...;
 * }
 * }
 * </pre>
 *
 * @author Security Commons Module
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAnyPermission {

    String[] value();

    String message() default "No tienes ninguno de los permisos requeridos";
}
