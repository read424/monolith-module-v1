package com.walrex.module_comercial.domain.exceptions;

/**
 * Excepción lanzada cuando una partida está desactivada.
 *
 * @author System
 * @version 0.0.1-SNAPSHOT
 */
public class PartidaDesactivadaException extends RuntimeException {

    public PartidaDesactivadaException(Integer idPartida) {
        super(String.format("La partida %d está desactivada", idPartida));
    }

    public PartidaDesactivadaException(String message) {
        super(message);
    }
}