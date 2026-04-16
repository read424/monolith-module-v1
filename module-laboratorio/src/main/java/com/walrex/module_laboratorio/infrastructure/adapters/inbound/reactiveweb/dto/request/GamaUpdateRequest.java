package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class GamaUpdateRequest {
    private String name;
    private Integer status;
    private Short order;
}
