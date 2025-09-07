package com.walrex.module_partidas.domain.model;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoAlmacen {

    private Integer idOrdeningreso;

    private Integer idCliente;

    private String codIngreso;

    private Integer idAlmacen;
    
    private Integer idArticulo;

    private Integer idUnidad;

    private Integer cntRollos;

    private Double pesoRef;

    private List<ItemRollo> rollos;
}
