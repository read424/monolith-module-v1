package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarKardexRequest {
    @JsonProperty("id_articulo")
    private Integer idArticulo;

    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    @JsonProperty("fecha_inicio")
    private LocalDate fechaInicio;

    @JsonProperty("fecha_fin")
    private LocalDate fechaFin;
}
