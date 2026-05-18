package com.walrex.module_partidas.domain.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartidaTypeaheadItem {

    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("cod_partida")
    private String codPartida;

    @JsonProperty("id_cliente")
    private Integer idCliente;

    @JsonProperty("id_receta")
    private Integer idReceta;

    @JsonProperty("cnt_rollo")
    private Integer cntRollo;

    @JsonProperty("total_kg")
    private Double totalKg;

    @JsonProperty("fec_entrega")
    private LocalDate fecEntrega;

    @JsonProperty("curva_diseno")
    private JsonNode curvaDiseno;

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("no_alias")
    private String noAlias;

    @JsonProperty("cod_receta")
    private String codReceta;

    @JsonProperty("desc_receta")
    private String descReceta;
}
