package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO de respuesta para declinación de salida de tacho
 * Contiene la información del ingreso al almacén de rechazo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclineOutTachoResponse {

    /**
     * ID de la orden de ingreso
     */
    @JsonProperty("id_orden_ingreso")
    private Integer idOrdenIngreso;

    /**
     * Código de ingreso generado
     */
    @JsonProperty("cod_ingreso")
    private String codIngreso;

    /**
     * ID del almacén de rechazo (siempre 6)
     */
    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    /**
     * ID del cliente
     */
    @JsonProperty("id_cliente")
    private Integer idCliente;

    /**
     * ID del artículo
     */
    @JsonProperty("id_articulo")
    private Integer idArticulo;

    /**
     * ID de la unidad
     */
    @JsonProperty("id_unidad")
    private Integer idUnidad;

    /**
     * Cantidad de rollos procesados
     */
    @JsonProperty("cnt_rollos")
    private Integer cntRollos;

    /**
     * Peso total de referencia
     */
    @JsonProperty("peso_ref")
    private Double pesoRef;

    /**
     * Lista de rollos procesados
     */
    private List<ItemRolloResponse> rollos;

    /**
     * Mapeo de ingresos por orden
     */
    private Map<Integer, Integer> ingresos;

    /**
     * Cantidad de rollos en el almacén
     */
    @JsonProperty("cnt_rollos_almacen")
    private Integer cntRollosAlmacen;

    /**
     * Motivo del rechazo
     */
    @JsonProperty("motivo_rechazo")
    private String motivoRechazo;

    /**
     * Personal supervisor
     */
    @JsonProperty("personal_supervisor")
    private String personalSupervisor;

    /**
     * Observación del rechazo
     */
    private String observacion;
}
