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
public class PartidaListItem {

    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("id_tipo_partida")
    private Integer idTipoPartida;

    @JsonProperty("cod_partida")
    private String codPartida;

    @JsonProperty("cnt_rollo")
    private Integer cntRollo;

    @JsonProperty("total_kg")
    private Double totalKg;

    @JsonProperty("id_cliente")
    private Integer idCliente;

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("desc_articulo")
    private String descArticulo;
}
