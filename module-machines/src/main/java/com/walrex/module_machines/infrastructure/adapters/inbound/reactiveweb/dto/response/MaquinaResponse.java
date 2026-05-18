package com.walrex.module_machines.infrastructure.adapters.inbound.reactiveweb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaquinaResponse {
    @JsonProperty("id_maquina")
    private Integer idMaquina;
    @JsonProperty("desc_maq")
    private String descMaq;
}
