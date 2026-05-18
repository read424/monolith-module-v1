package com.walrex.module_laboratorio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtapaTintura {
    private Integer id_tintura;
    private Integer id_proceso;
    private String desc_tintura;
    private String observacion;
    private LocalDate fec_registro;
    private Integer status;
    private Integer id_usuario;
}
