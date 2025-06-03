package com.walrex.module_almacen.application.ports.output;

import reactor.core.publisher.Mono;

public interface CompensacionTransaccionPort {
    /**
     * Revierte una transacción por su identificador
     *
     * @param transactionId Identificador de transacción a revertir
     * @return Mono que completa cuando se ha realizado la compensación
     */
    Mono<Void> compensarTransaccion(String transactionId);
}
