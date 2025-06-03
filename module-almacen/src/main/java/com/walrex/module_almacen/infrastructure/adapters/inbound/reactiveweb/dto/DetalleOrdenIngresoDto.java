package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetalleOrdenIngresoDto {
    private Integer idDetOrdenIngreso;

    @NotNull(message = "El artículo es obligatorio")
    private Integer idArticulo;

    @NotNull(message = "La unidad es obligatoria")
    private Integer idUnidad;

    @NotNull(message = "La unidad de salida es obligatoria")
    private Integer idUnidadSalida;

    private Integer idMoneda;

    @NotNull(message = "La cantidad es obligatoria")
    private BigDecimal cantidad;

    private Boolean excentoImp;

    @NotNull(message = "El costo es obligatorio")
    private BigDecimal costo;

    @NotNull(message = "El monto total es obligatorio")
    private BigDecimal mtoTotal;

    private List<DetalleRolloDto> detallesRollos;
}
