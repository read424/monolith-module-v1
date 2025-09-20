package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import lombok.*;

/**
 * DTO para motivo de rechazo
 * Contiene información del motivo por el cual se rechaza la salida de tacho
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoRechazo {

    /**
     * Valor del motivo de rechazo
     */
    private String value;

    /**
     * Texto descriptivo del motivo de rechazo
     */
    private String text;
}
