package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para el destinatario en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetDestinatario {

    private String tipoDoc;
    private String numDoc;
    private String rznSocial;
}