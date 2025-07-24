package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.reactiveweb.response;

import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPDFResponse {

    private String documentoId;
    private String tipoComprobante;
    private String nombreArchivo;
    private Long tamanoBytes;
    private String urlDescarga;
    private LocalDateTime fechaGeneracion;
    private String estado;
    private String mensaje;

    @Builder.Default
    private Boolean exitoso = true;
}