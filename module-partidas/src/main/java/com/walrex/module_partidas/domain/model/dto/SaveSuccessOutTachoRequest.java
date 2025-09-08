package com.walrex.module_partidas.domain.model.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO de request para el endpoint saveSuccessOutTacho
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSuccessOutTachoRequest {

    /**
     * ID de la partida
    */
    @JsonProperty("id_partida")
    private Integer idPartida;

    /**
     * ID del almacén
    */
    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    /**
     * Lote del detalle de orden de ingreso
    */
    @JsonProperty("lote")
    private String lote;

    
    @JsonProperty("id_cliente")
    private Integer idCliente;

    /**
     * Lista de detalles de rollos
    */
    private List<DetalleRollo> rollos;

    /**
     * DTO interno para los detalles de rollo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRollo {

        /**
         * Código del rollo
         */
        private String codRollo;

        /**
         * Peso del rollo
         */
        private String pesoRollo;

        /**
         * Peso acabado
         */
        private BigDecimal pesoAcabado;

        /**
         * ID del ingreso peso
         */
        private String idIngresopeso;

        /**
         * ID del detalle partida
         */
        private String idDetPartida;

        /**
         * ID del ingreso almacén
         */
        private String idIngresoAlmacen;

        /**
         * ID del rollo ingreso
         */
        private String idRolloIngreso;

        /**
         * Indica si está despachado
         */
        private Boolean despachado;

        /**
         * ID del almacén
         */
        private String idAlmacen;

        /**
         * ID de la orden de ingreso
         */
        private String idOrdeningreso;

        /**
         * Indica si está seleccionado
         */
        private Boolean selected;

        /**
         * Indica si está marcado para eliminar
         */
        private Integer delete;
    }
}
