package com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotificationRequest {
    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("id_ordeningreso")
    private Integer idOrdenIngreso;

    @JsonProperty("cod_ordeningreso")
    private String codOrdenIngreso;

    @JsonProperty("store_out")
    private String storeOut;

    @JsonProperty("id_ordeningreso_out")
    private Integer idOrdenIngresoOut;
}
