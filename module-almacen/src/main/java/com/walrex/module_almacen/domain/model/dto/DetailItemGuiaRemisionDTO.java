package com.walrex.module_almacen.domain.model.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailItemGuiaRemisionDTO {
    private Integer idDetalleOrden;
    private Integer idOrdenSalida;
    private Integer idProducto;
    private Integer idUnidad;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal total;
    private BigDecimal peso;
    private Integer idTipoServicio;
}
