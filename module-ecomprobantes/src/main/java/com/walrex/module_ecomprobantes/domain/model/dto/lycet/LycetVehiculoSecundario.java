package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para vehículos secundarios en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetVehiculoSecundario {

    private String placa;
}