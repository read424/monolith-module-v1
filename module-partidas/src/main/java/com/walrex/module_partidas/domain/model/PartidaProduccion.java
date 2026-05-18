package com.walrex.module_partidas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartidaProduccion {
    private Integer id;
    private Integer status;
    private Integer nivelCritico;
    private String descripcionMotivo;
    private Integer isObservado;
    private Integer rollosDeclarados;
    private Integer cntRollos;
    private LocalDate fecProgramacion;
    private Integer idTipoMaquina;
    private Integer idMaquina;
    private Integer idPartida;
    private String codPartida;
    private Integer idCliente;
    private String razonSocial;
    private String descArticulo;
    private String codReceta;
    private String descReceta;
    private String descMaq;
    private Long isVale;
    private LocalDate fecRealInicio;
    private LocalDate fecRealFin;
    private Integer isDeclarado;
}
