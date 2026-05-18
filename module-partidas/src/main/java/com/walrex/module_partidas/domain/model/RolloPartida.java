package com.walrex.module_partidas.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloPartida {

    @JsonProperty("id_det_partida")
    private Integer idDetPartida;

    @JsonProperty("id_detordeningresopeso")
    private Integer idDetordeningresopeso;

    private Integer status;

    @JsonProperty("cod_rollo")
    private String codRollo;

    @JsonProperty("peso_rollo")
    private Double pesoRollo;

    @JsonProperty("no_almacen")
    private String noAlmacen;

    @JsonProperty("id_liquidacion")
    private Integer idLiquidacion;
}
