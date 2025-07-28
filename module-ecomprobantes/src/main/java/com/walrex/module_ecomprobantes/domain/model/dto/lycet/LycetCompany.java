package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para la empresa en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetCompany {

    private String ruc;
    private String razonSocial;
    private String nombreComercial;
}