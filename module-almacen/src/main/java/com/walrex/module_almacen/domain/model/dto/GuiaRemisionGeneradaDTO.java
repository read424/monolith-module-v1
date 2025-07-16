package com.walrex.module_almacen.domain.model.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionGeneradaDTO {
    private Boolean isGuiaSunat;
    private Long idOrdenSalida;
    private Integer idMotivoComprobante;
    private Integer idModalidad;
    private Integer idEmpresaTransp;
    private Integer idTipDocChofer;
    private String numDocChofer;
    private String numPlaca;
    private Integer idLlegada;
    private LocalDate fechaEntrega;
    private Long idDevolucion;
    private Integer idComprobante;
    private Integer entregado;
    private Integer status;
    private Integer idCliente;
    private Integer idUsuario;
}