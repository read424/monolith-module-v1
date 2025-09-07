package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * Response DTO de response para almacén tacho
 * Incluye el campo timeElapsed calculado en milisegundos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePartidaTacho {
    /**
     * ID de la orden de ingreso
     */
    @JsonProperty("id_ordeningreso")
    private Integer idOrdeningreso;

    /**
     * ID del cliente
     */
    @JsonProperty("id_cliente")
    private Integer idCliente;

    /**
     * Razón social del cliente
     */
    @JsonProperty("razon_social")
    private String razonSocial;

    /**
     * Alias del cliente
     */
    @JsonProperty("no_alias")
    private String noAlias;

    /**
     * Fecha de registro
     */
    @JsonProperty("fec_registro")
    private LocalDateTime fecRegistro;

    /**
     * Código de ingreso
     */
    @JsonProperty("cod_ingreso")
    private String codIngreso;

    /**
     * ID del detalle de orden de ingreso
     */
    @JsonProperty("id_detordeningreso")
    private Integer idDetordeningreso;

    /**
     * ID de la partida
     */
    @JsonProperty("id_partida")
    private Integer idPartida;

    /**
     * Código de la partida
     */
    @JsonProperty("cod_partida")
    private String codPartida;

    /**
     * Cantidad de rollos
     */
    @JsonProperty("cnt_rollos")
    private Integer cntRollos;

    /**
     * Código de la receta
     */
    @JsonProperty("cod_receta")
    private String codReceta;

    /**
     * Nombre de los colores
     */
    @JsonProperty("no_colores")
    private String noColores;

    /**
     * ID del tipo de tenido
     */
    @JsonProperty("id_tipo_tenido")
    private Integer idTipoTenido;

    /**
     * Descripción del tenido
     */
    @JsonProperty("desc_tenido")
    private String descTenido;

    /**
     * Tiempo transcurrido en segundos desde fecRegistro hasta ahora
     */
    @JsonProperty("time_elapsed")
    private Long timeElapsed;
}
