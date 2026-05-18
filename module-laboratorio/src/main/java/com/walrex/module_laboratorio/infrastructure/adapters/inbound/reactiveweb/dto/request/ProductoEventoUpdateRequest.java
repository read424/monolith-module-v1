package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEventoUpdateRequest {
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    private Integer status;
}
