package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetalleRolloDto {
    @NotNull(message = "El código del rollo es obligatorio")
    private String codRollo;

    @NotNull(message = "El peso del rollo es obligatorio")
    private BigDecimal pesoRollo;

    private Integer ordenIngreso;
    private Integer idDetOrdenIngreso;
}
