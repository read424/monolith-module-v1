package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para el chofer en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetChofer {

    private String tipo;
    private String tipoDoc;
    private String nroDoc;
    private String licencia;
    private String nombres;
    private String apellidos;
}