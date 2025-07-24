package com.walrex.module_ecomprobantes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentModel {
    private String codTraslado;
    private String modTraslado;
    private LocalDate fechaTraslado;
    private Double pesoTotal;
    private String unidadPeso;
    private AddressModel llegada;
    private AddressModel partida;
    private CarrierModel transportista;
}
