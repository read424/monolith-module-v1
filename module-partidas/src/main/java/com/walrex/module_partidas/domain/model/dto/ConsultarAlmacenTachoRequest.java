package com.walrex.module_partidas.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO de request para consultar almacén tacho
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarAlmacenTachoRequest {

    /**
     * Número de página (opcional)
     */
    private Integer page;

    /**
     * ID del almacén (obligatorio)
     */
    @NotNull(message = "El ID del almacén es obligatorio")
    @Positive(message = "El ID del almacén debe ser un número positivo")
    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    /**
     * Número de filas por página (opcional)
     */
    @Positive(message = "El número de filas debe ser un número positivo")
    @JsonProperty("num_rows")
    private Integer numRows;

    /**
     * Total de páginas (opcional)
     */
    @JsonProperty("total_pages")
    private Integer totalPages;

    /**
     * Código de partida para búsqueda (opcional)
     * Permite buscar partidas por código específico o parcial
     */
    @JsonProperty("cod_partida")
    private String codPartida;
}
