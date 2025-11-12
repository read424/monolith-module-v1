package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionItemProjection {
    private Integer idProducto;
    private Integer idOrdensalida;
    private Float cantidad;
    private Float precio;
    private Float subtotal;
    private Integer idDetalleOrden;
    private Float peso;
    private Integer idUnidad;
    private Integer tipoServicio;
}
