package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoSalidaDTO {
    private Integer id_detalle_orden;
    @NotNull(message = "campo obligatorio")
    private Integer id_articulo;
    private String desc_articulo;
    private String abrev_unidad;
    @NotNull(message = "")
    private Double cantidad;
    private Boolean selected; // ✅ Campo clave para filtrar
    private String delete;
    private Integer id_unidad_consumo;
    private Integer id_unidad;
    private Integer id_unidad_old;
}
