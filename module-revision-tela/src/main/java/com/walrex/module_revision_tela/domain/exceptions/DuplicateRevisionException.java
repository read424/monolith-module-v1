package com.walrex.module_revision_tela.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta crear una revisión duplicada
 * Ocurre cuando ya existe una revisión para la misma orden y periodo
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public class DuplicateRevisionException extends RevisionInventarioException {

    public DuplicateRevisionException(Integer idOrdeningreso, Integer idPeriodo) {
        super(String.format("Ya existe una revisión para la orden %d en el periodo %d",
            idOrdeningreso, idPeriodo));
    }

    public DuplicateRevisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
