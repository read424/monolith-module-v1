package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import java.util.List;

import lombok.*;

/**
 * DTO para el vehículo en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetVehiculo {

    private String placa;
    private List<LycetVehiculoSecundario> secundarios;
}