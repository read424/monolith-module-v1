package com.walrex.module_almacen.domain.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionGeneradaDTO {
    private Long idDevolucion;
    private Long idOrdenSalida;
    private Integer idMotivo;
    private Integer idComprobante;
    private LocalDate fechaEntrega;
    private Integer idEmpresaTransp;
    private Integer idModalidad;
    private Integer idTipDocChofer;
    private String numDocChofer;
    private String numPlaca;
    private Integer idLlegada;
    private Integer entregado;
    private Integer status;
    private Integer idUsuario;
    private Integer idCliente;
    private List<DetailItemGuiaRemisionDTO> detailItems;
}