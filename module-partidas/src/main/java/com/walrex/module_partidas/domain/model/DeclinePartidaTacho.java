package com.walrex.module_partidas.domain.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Modelo de dominio para declinar salida de tacho
 * Representa los datos necesarios para procesar el rechazo de una salida de tacho
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclinePartidaTacho {

    /**
     * ID de la partida
     */
    @NotNull(message = "El id partida es obligatorio")
    private Integer idPartida;

    /**
     * ID del almacén
     */
    @NotNull(message = "El id almacen es obligatorio")
    private Integer idAlmacen;

    /**
     * ID del cliente
     */
    @NotNull(message = "El id cliente es obligatorio")
    private Integer idCliente;
    
    /**
     * ID del artículo
     */
    @NotNull(message = "El id articulo es obligatorio")
    private Integer idArticulo;

    /**
     * Lote
     */
    @NotNull(message = "El lote es obligatorio")
    private String lote;

    /**
     * ID de la unidad
     */
    @NotNull(message = "El id unidad es obligatorio")
    private Integer idUnidad;

    /**
     * Nivel de observación
     */
    private Integer nivelObservacion;

    /**
     * Motivo de rechazo
     */
    @Valid
    @NotNull(message = "El motivo de rechazo es obligatorio")
    private MotivoRechazoDomain motivoRechazo;

    /**
     * ID del Usuario
     */
    @NotNull(message = "El id usuario es obligatorio")
    private Integer idUsuario;

    /**
     * Personal supervisor
     */
    @Valid
    @NotNull(message = "El personal supervisor es obligatorio")
    private PersonalSupervisorDomain personal;

    /**
     * Observación
     */
    private String observacion;

    /**
     * Lista de rollos
     */
    @Valid
    private List<ItemRolloProcess> rollos;
}
