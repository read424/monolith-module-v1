package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSalidaTacho {

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
     * ID del cliente
    */
    @JsonProperty("id_cliente")
    private Integer idCliente;

    /**
     * ID del artículo
    */
    private Integer idArticulo;

    /**
     * Lote
    */
    private String lote;

    /**
     * ID de la unidad
    */
    private Integer idUnidad;

    /**
     * Nivel de observación
    */
    private Integer nivelObservacion;

    /**
     * Motivo de rechazo
    */
    @JsonProperty("motivo_rechazo")
    private Integer idMotivoRechazo;

    /**
     * ID del supervisor
    */
    @JsonProperty("id_supervisor")
    private Integer idSupervisor;

    /**
     * Observación
    */
    private String observacion;

    /**
     * Lista de rollos
    */
    private List<RolloTacho> rollos;
}
