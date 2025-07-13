package com.walrex.module_almacen.domain.model.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionGeneradaDTO {

    private Long idOrdenSalida;
    private String codigoSalida;
    private LocalDate fechaEntrega;
    private Integer idEmpresaTransp;
    private Integer idModalidad;
    private Integer idTipDocChofer;
    private String numDocChofer;
    private String numPlaca;
    private Integer idLlegada;
    private Integer idComprobante;
    private Integer entregado;
    private Integer status;
    private Integer idUsuario;
}