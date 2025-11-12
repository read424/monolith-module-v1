package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuiaGeneradaUpdatedEvent {
    private Boolean success;
    private Integer idOrdenSalida;
    private Integer idComprobante;
    private String codigoComprobante;
}
