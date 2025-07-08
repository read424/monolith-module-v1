package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.SalidaDevolucionDTO;

import reactor.core.publisher.Mono;

/**
 * Use Case para registrar devolución de rollos
 * Puerto de entrada para la funcionalidad de devolución de rollos sin cobro
 */
public interface RegistrarDevolucionRollosUseCase {

    /**
     * Registra una devolución de rollos procesando:
     * 1. Validación de rollos (sinCobro=0 y Reproceso)
     * 2. Creación de orden de salida
     * 3. Registro de detalles de peso
     * 4. Trazabilidad en tabla devolucion_rollos
     * 5. Actualización de status de rollos
     * 
     * @param devolucionRollos DTO con datos de la devolución
     * @param idUsuario        ID del usuario que realiza la devolución
     * @return Mono con el resultado de la operación
     */
    Mono<SalidaDevolucionDTO> registrarDevolucionRollos(SalidaDevolucionDTO devolucionRollos, Integer idUsuario);
}