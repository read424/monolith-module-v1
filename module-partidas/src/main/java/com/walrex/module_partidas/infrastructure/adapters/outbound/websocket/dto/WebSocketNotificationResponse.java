package com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotificationResponse {
    private String message;
    private WebSocketNotificationRequest data;
}
