package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

/**
 * DTO para la solicitud de guía de remisión a la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetGuiaRemisionRequest {

    private String version;
    private String tipoDoc;
    private String serie;
    private String correlativo;
    private String observacion;
    private LocalDateTime fechaEmision;
    private LycetCompany company;
    private LycetDestinatario destinatario;
    private LycetEnvio envio;
    private List<LycetDetail> details;
}