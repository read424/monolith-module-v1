package com.walrex.module_partidas.domain.model.dto;

import lombok.*;

/**
 * DTO de request para consultar los procesos de una partida
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarProcesosPartidaRequest {

    /**
     * ID de la partida a consultar
     */
    private Integer idPartida;
}
