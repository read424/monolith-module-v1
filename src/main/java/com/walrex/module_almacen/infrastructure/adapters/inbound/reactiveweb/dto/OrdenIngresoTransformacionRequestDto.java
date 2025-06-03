package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrdenIngresoTransformacionRequestDto {
    @NotNull(message = "Campo id_articulo es obligatorio")
    private Integer idArticulo;
    @NotNull(message = "Campo cantidad es obligatorio")
    private Double cantidad;
    @NotNull(message = "Campo precio es obligatorio")
    private Double precio;
    @Valid
    private UnidadMedidaRequest unidad;
    @JsonProperty("fec_ingreso")
    private LocalDate fecIngreso;
    private List<ItemArticuloRequest> detalles;
}