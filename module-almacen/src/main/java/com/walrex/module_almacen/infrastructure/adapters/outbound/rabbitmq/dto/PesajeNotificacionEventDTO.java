package com.walrex.module_almacen.infrastructure.adapters.outbound.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesajeNotificacionEventDTO {
    private Integer id_detordeningreso;
    private String cod_rollo;
    private Double peso_rollo;
    private Integer cnt_registrados;
    private Boolean completado;
}
