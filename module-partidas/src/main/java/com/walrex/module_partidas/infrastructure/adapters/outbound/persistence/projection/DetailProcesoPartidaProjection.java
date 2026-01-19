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
public class DetailProcesoPartidaProjection {

    private Integer id_partida;

    private Integer id_det_ruta;

    private Integer id_proceso;

    private String no_proceso;

    private Integer id_tipo_maquina;

    private Integer isservicio;

    private Integer id_partida_maquina;

    private Integer id_maquina;

    private LocalDate fec_real_inicio;

    private String hora_inicio;

    private LocalDate fec_real_fin;

    private String hora_fin;

    private Integer id_nivel_observ;

    private Integer is_main_proceso;

    private Integer status;

    private String observacion;
}
