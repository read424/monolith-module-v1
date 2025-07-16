package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

/**
 * DTO que representa los datos de una serie de comprobantes.
 * 
 * Utilizado para transferir información de series entre capas de la aplicación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoSerieDTO {

    /**
     * Identificador único de la serie
     */
    private Integer idSerie;

    /**
     * Número de serie (código alfanumérico)
     */
    private String nuSerie;

    /**
     * Estado de la serie (true = activo, false = inactivo)
     */
    private Boolean ilEstado;

    /**
     * Identificador del tipo de comprobante asociado
     */
    private Integer idCompro;

    /**
     * Número del comprobante
     */
    private Integer nuCompro;

    /**
     * Indicador si es un comprobante electrónico (CPE)
     * 0 = No es CPE, 1 = Es CPE
     */
    private Integer isCpe;
}