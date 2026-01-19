package com.walrex.module_revision_tela.domain.exceptions;

/**
 * Excepción lanzada cuando no hay suficientes rollos disponibles para asignar
 * a un registro de levantamiento
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public class InsufficientRollosException extends RevisionInventarioException {

    private final Integer idLevantamiento;
    private final Integer rollosRequeridos;
    private final Integer rollosDisponibles;

    public InsufficientRollosException(Integer idLevantamiento, Integer rollosRequeridos, Integer rollosDisponibles) {
        super(String.format(
            "Rollos insuficientes para levantamiento ID %d: se requieren %d rollos pero solo hay %d disponibles",
            idLevantamiento,
            rollosRequeridos,
            rollosDisponibles
        ));
        this.idLevantamiento = idLevantamiento;
        this.rollosRequeridos = rollosRequeridos;
        this.rollosDisponibles = rollosDisponibles;
    }

    public Integer getIdLevantamiento() {
        return idLevantamiento;
    }

    public Integer getRollosRequeridos() {
        return rollosRequeridos;
    }

    public Integer getRollosDisponibles() {
        return rollosDisponibles;
    }
}
