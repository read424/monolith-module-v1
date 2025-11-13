package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para consultar orden de producción por partida
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenProduccionPartidaRequest {

    private Integer id_partida;
}