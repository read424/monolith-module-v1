package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DetalleOrdenIngreso {
    private Integer id;
    private Articulo articulo;
    private Integer idDetalleOrdenCompra;
    private String lote;
    private Integer idTipoProducto;
    private Integer idTipoProductoFamilia;
    private Integer idUnidad;
    private Integer idUnidadSalida;
    private Integer idMoneda;
    private BigDecimal cantidad;
    private Boolean excentoImp;
    private BigDecimal costo;
    private BigDecimal montoTotal;
    private BigDecimal cantidadSaldo;
    private Integer idLoteInventario;
    private List<DetalleRollo> detallesRollos;
}
