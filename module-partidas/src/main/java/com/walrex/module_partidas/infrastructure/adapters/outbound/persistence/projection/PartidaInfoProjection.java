package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartidaInfoProjection {

    private Integer id_partida;

    private String cod_partida;

    private Integer cnt_rollo;

    private Double total_kg;

    private Integer id_ruta;

    private Integer id_receta;

    private Integer status;

    private Integer id_cliente;

    private Integer id_ordenproduccion;

    private Integer id_tipo_acabado;

    private Integer id_receta_acabado;

    private String color_referencia;

    private Integer id_articulo;

    private Integer id_partida_parent;

    private Integer num_reprocesos;

    private Integer condition;

    private LocalDate fec_programado;

    private Integer add_cobro;

    private Integer type_reprocess;

    private String lote;
}
