package com.walrex.module_almacen.domain.model.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloDevolucionDTO {
    private Integer idDetOrdenSalidaPeso;
    private Integer idOrdenSalida;
    private Integer idDetalleOrden;
    private String codRollo;
    private BigDecimal pesoRollo;
    private Integer idRolloIngreso;
    private Integer idDetOrdenIngreso;
    private Integer idDetOrdenIngresoPeso;
    private String statusRollIngreso;
    private Integer idPartida;
    private Integer idDetPartida;
    private String sinCobro;
    private String statusRollPartida;
    private Integer idRolloIngresoAlmacen;
    private Integer idDetOrdenIngresoAlmacen;
    private Integer idDetOrdenIngresoPesoAlmacen;
    private String statusRollIngresoPesoAlmacen;
    private Boolean selected;
    private Boolean delete;
}
