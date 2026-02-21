package com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PesajeNotificacionEventDTO {
    private Integer id_detordeningreso;
    private String cod_rollo;
    private Double peso_rollo;
    private Integer cnt_registrados;
    private Boolean completado;
}
