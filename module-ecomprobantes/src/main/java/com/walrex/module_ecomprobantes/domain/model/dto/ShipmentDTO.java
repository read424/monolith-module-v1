package com.walrex.module_ecomprobantes.domain.model.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentDTO {
    private String codTraslado;
    private String descTraslado;
    private String modTraslado;
    private Integer numBultos;
    private Double pesoTotal;
    private String unidadPeso;
    private LocalDate fecTraslado;
    private CarrierDTO transportista;
    private VehiclesDTO vehiculos;
    private DriverDTO conductor;
    private DireccionDTO llegada;
    private DireccionDTO partida;
}
