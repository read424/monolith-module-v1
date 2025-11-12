package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartidasStatusDespachoDTO {
    private Integer idPartida;
    private Integer cntDespachado;
}
