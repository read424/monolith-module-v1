package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request para cada rollo en la devolución
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloDevolucionRequest {

    @NotBlank(message = "El código del rollo es obligatorio")
    private String codRollo;

    @NotNull(message = "El peso del rollo es obligatorio")
    @Positive(message = "El peso del rollo debe ser positivo")
    private BigDecimal pesoRollo;

    @NotNull(message = "El ID del rollo de ingreso es obligatorio")
    private Integer idRolloIngreso;

    @NotNull(message = "El ID de partida es obligatorio")
    private Integer idPartida;

    @NotNull(message = "El ID de detalle de partida es obligatorio")
    private Integer idDetPartida;

    @NotNull(message = "El campo isCobro de partida es obligatorio")
    private String isCobro;

    @NotNull(message = "El campo Status de partida es obligatorio")
    private String statusPartida;

    private Integer idOrdenIngreso;

    private Integer idDetOrdenIngreso;

    private Integer idDetOrdenIngresoPeso;

    private Integer idAlmacen;

    private String statusAlmacen;
}