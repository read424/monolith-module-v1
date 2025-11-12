package com.walrex.module_almacen.domain.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionArticuloDTO {
    private Integer idOrdenSalida;
    private Integer idDetOrdenSalida;
    private Integer idArticulo;
    private String codArticulo;
    private String descArticulo;
    private String statusArticulo;
    private Integer idUnidad;
    private Integer cantidad;
    private BigDecimal totalPeso;
    private List<RolloDevolucionDTO> rollos;
}
