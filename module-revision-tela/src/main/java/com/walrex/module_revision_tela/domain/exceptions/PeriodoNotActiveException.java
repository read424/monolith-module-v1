package com.walrex.module_revision_tela.domain.exceptions;

/**
 * Excepción lanzada cuando no hay un periodo activo para procesar revisiones
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public class PeriodoNotActiveException extends RevisionInventarioException {

    public PeriodoNotActiveException() {
        super("No existe un periodo activo (status=1) para procesar revisiones de inventario");
    }
}
