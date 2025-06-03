package com.walrex.module_almacen.domain.model.dto;

import com.walrex.module_almacen.domain.model.Almacen;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrdenIngresoTransformacionDTO {
    private ArticuloTransformacionDTO articulo;
    private Double cantidad;
    private Double precio;
    private Almacen almacen;
    private UnidadMedidaDTO unidad_ingreso;
    private LocalDate fec_ingreso;
    private List<ItemArticuloTransformacionDTO> detalles;
}
