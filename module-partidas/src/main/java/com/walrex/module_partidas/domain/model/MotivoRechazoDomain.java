package com.walrex.module_partidas.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Modelo de dominio para motivo de rechazo
 * Representa la información del motivo por el cual se rechaza la salida de tacho
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoRechazoDomain {

    /**
     * Valor del motivo de rechazo
     */
    @NotBlank(message = "El valor del motivo de rechazo es obligatorio")
    private String value;

    /**
     * Texto descriptivo del motivo de rechazo
     */
    @NotBlank(message = "El texto del motivo de rechazo es obligatorio")
    private String text;
}
