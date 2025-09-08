package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloTacho {
    
    @JsonProperty("id_ordeningreso")
    private Integer idOrdenIngreso;//id_ordeningreso - almacen crudo

    @JsonProperty("id_ingresopeso")
    private Integer idIngresoPeso;//id_detordeningresopeso - almacen crudo

    @JsonProperty("cod_rollo")
    private String codRollo;

    @JsonProperty("peso_acabado")
    private Double pesoRollo;

    @JsonProperty("id_ingreso_almacen")
    private Integer idIngresoAlmacen;//id_ordeningreso - almacen produccion

    @JsonProperty("id_rollo_ingreso")
    private Integer idRolloIngreso;//id_detordeningresopeso - almacen produccion

    @JsonProperty("id_almacen")
    private Integer idAlmacen;//id_almacen - almacen produccion

    @JsonProperty("id_detpartida")
    private Integer idDetPartida;

    @JsonProperty("selected")
    private Boolean selected;

    @JsonProperty("delete")
    private Integer delete;

    @JsonProperty("status")
    private Integer status;
}
