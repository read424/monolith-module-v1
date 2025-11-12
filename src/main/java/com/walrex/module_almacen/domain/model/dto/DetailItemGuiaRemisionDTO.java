package com.walrex.module_almacen.domain.model.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailItemGuiaRemisionDTO {
    private Integer idProducto;
    private Integer idOrdenSalida;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal total;
    private Integer idDetalleOrden;
    private BigDecimal peso;
    private Integer idUnidad;
    private Integer idTipoServicio;
}
