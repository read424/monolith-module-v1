package com.walrex.module_almacen.infrastructure.adapters.outbound.websocket.dto;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketPesajeNotification {
    private String tipo;
    private PesajeDetalle data;
}
