package com.walrex.module_comercial.domain.exceptions;

/**
 * Excepción lanzada cuando una partida está parcialmente despachada.
 *
 * @author System
 * @version 0.0.1-SNAPSHOT
 */
public class PartidaParcialmenteDespachadaException extends RuntimeException {

    public PartidaParcialmenteDespachadaException(Integer idPartida) {
        super(String.format("Partida %d parcialmente despachada", idPartida));
    }

    public PartidaParcialmenteDespachadaException(String message) {
        super(message);
    }
}