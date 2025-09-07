package com.walrex.module_partidas.domain.model.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO de response para almacén tacho
 * Incluye el campo timeElapsed calculado en milisegundos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartidaTachoResponse {

    /**
     * ID de la orden de ingreso
     */
    private Integer idOrdeningreso;

    /**
     * ID del cliente
     */
    private Integer idCliente;

    /**
     * Razón social del cliente
     */
    private String razonSocial;

    /**
     * Alias del cliente
     */
    private String noAlias;

    /**
     * Fecha de registro
     */
    private LocalDateTime fecRegistro;

    /**
     * Código de ingreso
     */
    private String codIngreso;

    /**
     * ID del detalle de orden de ingreso
     */
    private Integer idDetordeningreso;

    /**
     * ID de la partida
     */
    private Integer idPartida;

    /**
     * Código de la partida
     */
    private String codPartida;

    /**
     * Cantidad de rollos
     */
    private Integer cntRollos;

    /**
     * Código de la receta
     */
    private String codReceta;

    /**
     * Nombre de los colores
     */
    private String noColores;

    /**
     * ID del tipo de tenido
     */
    private Integer idTipoTenido;

    /**
     * Descripción del tenido
     */
    private String descTenido;

    /**
     * Tiempo transcurrido en segundos desde fecRegistro hasta ahora
     */
    private Long timeElapsed;
}
