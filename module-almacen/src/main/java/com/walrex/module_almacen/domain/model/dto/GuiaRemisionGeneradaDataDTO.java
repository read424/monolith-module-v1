package com.walrex.module_almacen.domain.model.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuiaRemisionGeneradaDataDTO {
    private Integer idOrdenSalida;
    private Integer idCliente;
    private Integer idMotivo;
    private String fechaEmision;
    private Integer tipoComprobante;
    private Integer tipoSerie;
    private Integer idUsuario;
    private List<DetailItemGuiaRemisionDTO> detailItems;
}