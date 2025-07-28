package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para la dirección en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetDireccion {

    private String ubigueo;
    private String direccion;
}