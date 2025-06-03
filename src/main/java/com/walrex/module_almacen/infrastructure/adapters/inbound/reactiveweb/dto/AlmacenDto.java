package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenDto {
    @NotNull(message = "El ID del almacén es obligatorio")
    private Integer idAlmacen;

    @NotNull(message = "El tipo de almacén es obligatorio")
    private Integer tipoAlmacen;
}
