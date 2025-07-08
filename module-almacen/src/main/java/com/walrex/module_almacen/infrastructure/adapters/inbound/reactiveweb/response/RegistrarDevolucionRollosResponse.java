package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import lombok.*;

/**
 * Response simplificado para el registro de devolución de rollos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarDevolucionRollosResponse {

    private String codSalida;
    private Double totalKg;
    private Integer totalRollos;
}