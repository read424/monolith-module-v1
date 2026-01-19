package com.walrex.module_revision_tela.domain.exceptions;

/**
 * Excepción base para errores en el proceso de revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public class RevisionInventarioException extends RuntimeException {

    public RevisionInventarioException(String message) {
        super(message);
    }

    public RevisionInventarioException(String message, Throwable cause) {
        super(message, cause);
    }
}
