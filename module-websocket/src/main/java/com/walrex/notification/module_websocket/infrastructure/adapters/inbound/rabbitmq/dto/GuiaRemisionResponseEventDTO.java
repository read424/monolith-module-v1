package com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuiaRemisionResponseEventDTO {
    private Boolean success;
    private String message;
    private ResponseEventGuiaDataDto data;
}
