package com.walrex.module_revision_tela.domain.exceptions;

/**
 * Excepción lanzada cuando no hay datos para procesar en la revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public class NoDataToProcessException extends RevisionInventarioException {

    public NoDataToProcessException() {
        super("No se encontraron órdenes de ingreso pendientes (status_nuevo=10) para procesar");
    }
}
