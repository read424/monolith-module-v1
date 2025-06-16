package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarKardexRequest {
    @JsonProperty("id_arriculo")
    private Integer idArticulo;

    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    @JsonProperty("fecha_inicio")
    private LocalDate fechaInicio;

    @JsonProperty("fecha_fin")
    private LocalDate fechaFin;
}
