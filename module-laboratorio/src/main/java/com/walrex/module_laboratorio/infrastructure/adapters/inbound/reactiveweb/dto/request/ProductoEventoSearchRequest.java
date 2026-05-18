package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEventoSearchRequest {
    private String search;

    @NotNull(message = "El page es obligatorio")
    @Min(value = 0, message = "El page no puede ser menor que 0")
    private Integer page;

    @NotNull(message = "El size es obligatorio")
    @Positive(message = "El size debe ser mayor que 0")
    private Integer size;

    private Integer status;
}
