package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

/**
 * Proyección para los procesos de una partida
 * Basada en el SQL con múltiples JOINs para obtener información completa
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoPartidaProjection {

    /**
     * ID del cliente
     */
    private Integer idCliente;

    /**
     * ID de la partida
     */
    private Integer idPartida;

    /**
     * ID de la partida máquina
     */
    private Integer idPartidaMaquina;

    /**
     * ID de la ruta
     */
    private Integer idRuta;

    /**
     * ID del artículo
     */
    private Integer idArticulo;

    /**
     * ID del proceso
     */
    private Integer idProceso;

    /**
     * ID del detalle de ruta
     */
    private Integer idDetRuta;

    /**
     * Número del proceso
     */
    private String noProceso;

    /**
     * ID del almacén
     */
    private Integer idAlmacen;

    /**
     * ID de la máquina
     */
    private Integer idMaquina;

    /**
     * ID del tipo de máquina
     */
    private Integer idTipoMaquina;

    /**
     * Indica si el proceso ha sido iniciado
     */
    private Boolean iniciado;

    /**
     * Indica si el proceso ha sido finalizado
     */
    private Boolean finalizado;

    /**
     * Indica si el proceso está pendiente
     */
    private Boolean isPendiente;

    /**
     * Status del proceso
     */
    private Integer status;

    /**
     * Indica si es el proceso principal
     */
    private Boolean isMainProceso;

    /**
     * Descripción de la máquina
     */
    private String descMaq;
}
