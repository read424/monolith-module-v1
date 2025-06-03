package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRolloRequestDto {
    @JsonProperty("id_detordeningresopeso")
    private Integer id;
    private String cod_rollo;
    private Double peso_rollo;
    private Integer delete;
}
