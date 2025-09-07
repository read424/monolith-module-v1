package com.walrex.module_partidas.domain.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO de request para consultar detalle de ingreso con rollos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarDetalleIngresoRequest {

    /**
     * ID de la partida (obligatorio)
     */
    @NotNull(message = "El ID de la partida es obligatorio")
    @Positive(message = "El ID de la partida debe ser un número positivo")
    private Integer idPartida;

    /**
     * ID del almacén (obligatorio)
     */
    @NotNull(message = "El ID del almacén es obligatorio")
    @Positive(message = "El ID del almacén debe ser un número positivo")
    private Integer idAlmacen;
}
