package com.walrex.notification.module_websocket.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MsgWSGuiaGenerada {
    private Integer idOrdenSalida;
    private Integer idComprobante;
    private String codigoComprobante;
    private String message;
}
