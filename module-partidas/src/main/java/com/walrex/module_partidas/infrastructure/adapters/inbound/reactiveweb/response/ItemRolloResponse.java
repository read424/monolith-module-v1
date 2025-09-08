package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ItemRolloResponse {

    @JsonProperty("cod_rollo")
    private String codRollo;

    @JsonProperty("id_detordeningresopeso")
    private Integer idDetordeningresopeso;

    @JsonProperty("id_rollo_ingreso")
    private Integer idRolloIngreso;

    @JsonProperty("peso_rollo")
    private Double pesoRollo;

}
