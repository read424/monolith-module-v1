package com.walrex.role.module_role.application.ports.input;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para consumir mensajes
 * Siguiendo el principio de inversión de dependencias de SOLID
 */
public interface MessageConsumer<T> {
    /**
     * Consume un mensaje del tópico especificado
     * @param topic Tópico del que consumir mensajes
     * @return Mono que completa cuando se ha configurado el consumidor
     */
    Mono<Void> consumeMessages(String topic);
}
