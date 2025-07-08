package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.SalidaDevolucionDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para registrar devoluciones de rollos
 */
public interface DevolucionRollosPort {

    /**
     * Registra una devolución de rollos creando:
     * 1. Orden de salida
     * 2. Detalles de artículos
     * 3. Detalles de peso de rollos
     * 4. Trazabilidad en tabla devolucion_rollos
     * 
     * @param devolucionRollos datos de la devolución a registrar
     * @return Mono con la devolución registrada con IDs generados
     */
    Mono<SalidaDevolucionDTO> registrarDevolucionRollos(SalidaDevolucionDTO devolucionRollos);

    /**
     * Verifica si un rollo ya fue devuelto anteriormente
     * 
     * @param idDetOrdenIngresoPeso ID del detalle de rollo de ingreso
     * @return Mono con true si ya fue devuelto, false si no
     */
    Mono<Boolean> verificarRolloYaDevuelto(Integer idDetOrdenIngresoPeso);
}