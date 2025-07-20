package com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEventGuiaDataDto {
    private Integer idOrdenSalida;
    private Integer idComprobante;
    private String codigoComprobante;
}
