package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecetaResponse {
    private Integer id;
    private String codReceta;
    private String razonSocial;
    private String codColores;
    private String noColores;
    private Integer status;
    private Boolean compartir;
    private String noGama;
    private String noColor;
    private String noTenido;
}
