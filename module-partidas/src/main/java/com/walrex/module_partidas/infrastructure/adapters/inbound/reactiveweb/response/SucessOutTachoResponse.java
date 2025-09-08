package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucessOutTachoResponse {
    
    private String message;

    @JsonProperty("id_ordeningreso")
    private Integer idOrdenIngreso;

    @JsonProperty("cod_ingreso")
    private String codIngreso;

    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    @JsonProperty("id_articulo")
    private Integer idArticulo;

    @JsonProperty("id_unidad")
    private Integer idUnidad;

    @JsonProperty("cnt_rollos")
    private Integer cntRollos;

    @JsonProperty("peso_ref")
    private Double pesoRef;

    @JsonProperty("rollos")
    private List<ItemRolloResponse> rollos;
}
