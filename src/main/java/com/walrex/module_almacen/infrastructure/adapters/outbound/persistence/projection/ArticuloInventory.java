package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticuloInventory {
    private Integer idArticulo;
    private Integer valorConv;
    private String isMultiplo;
    private Integer idTipoProducto;
    private Integer idUnidad;
    private Integer idUnidadConsumo;
    private BigDecimal stock;
}
