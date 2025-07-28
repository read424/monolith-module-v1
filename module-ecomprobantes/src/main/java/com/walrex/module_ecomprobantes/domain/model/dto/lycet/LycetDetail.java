package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para los detalles en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetDetail {

    private String codigo;
    private String descripcion;
    private String unidad;
    private Integer cantidad;
}