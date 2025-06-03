package com.walrex.module_almacen.domain.model.dto;

import com.walrex.module_almacen.domain.model.Articulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEgresoDTO {
    private Long id;
    private Long idOrdenEgreso;
    private Articulo articulo;
    private Integer idUnidad;
    private Double cantidad;
    private Integer entregado;
    private Double totalKilos;
    private Double precio;
    private BigDecimal totalMonto;
    private String observacion;
    private Integer status;
    private List<LoteDTO> a_lotes;
}
