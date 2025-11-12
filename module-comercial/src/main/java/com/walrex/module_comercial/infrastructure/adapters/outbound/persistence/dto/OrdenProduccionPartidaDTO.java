package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de orden de producción asociada a una partida.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProduccionPartidaDTO{
    private Integer idPartida;
    private Integer condition;
    private Integer status;
    private Integer idOrdenProduccion;
    private String codOrdenProduccion;
    private Integer idOrden;
    private Integer idDetOs;
    private Integer idRuta;
    private Integer idGama;
    private Integer idOrdenIngreso;
    private Integer idPrecio;
    private Double precio;
    private String descArticulo;
}
