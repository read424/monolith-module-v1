package com.walrex.module_partidas.domain.exceptions;

/**
 * Excepción específica para errores de persistencia de órdenes de salida
 */
public class OrdenSalidaPersistenceException extends RuntimeException {

    public OrdenSalidaPersistenceException(String message) {
        super(message);
    }

    public OrdenSalidaPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
