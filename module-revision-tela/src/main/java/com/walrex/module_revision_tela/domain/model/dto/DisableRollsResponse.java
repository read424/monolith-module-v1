package com.walrex.module_revision_tela.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisableRollsResponse {
    private Integer idPeriodo;
    private Integer totalRollosProcesados;
    private Integer rollosAlmacenDeshabilitados;
    private Integer rollosGuiaDeshabilitados;
    private String mensaje;
}
