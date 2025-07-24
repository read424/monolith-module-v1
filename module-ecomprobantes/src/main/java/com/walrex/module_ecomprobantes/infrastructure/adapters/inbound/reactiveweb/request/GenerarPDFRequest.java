package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.reactiveweb.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPDFRequest {

    @NotBlank(message = "El Id comprobante es obligatorio")
    private Integer idComprobante;
}