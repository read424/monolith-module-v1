package com.walrex.module_comercial.domain.exceptions;

/**
 * Excepción lanzada cuando una partida no está habilitada para proceder con el cambio.
 *
 * @author System
 * @version 0.0.1-SNAPSHOT
 */
public class PartidaNoHabilitadaException extends RuntimeException {

    public PartidaNoHabilitadaException(Integer idPartida, Integer status) {
        super(String.format("La partida %d con status %d no se encuentra habilitada para proceder con el cambio", idPartida, status));
    }

    public PartidaNoHabilitadaException(String message) {
        super(message);
    }
}