package com.walrex.module_partidas.domain.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRolloProcessDTO {

    private String codRollo;

    private Double pesoRollo;

    private Integer idOrdenIngreso;

    private Integer idIngresoPeso;

    private Integer idIngresoAlmacen;

    private Integer idRolloIngreso;

    private Integer idDetPartida;

    private Integer idAlmacen;

    private Integer idDetOrdenIngPesoAlmacen;

    private Boolean selected;

    private Integer status;
}
