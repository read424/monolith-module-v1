package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @NotNull(message = "El ID Orden de Ingreso es obligatorio")
    @JsonProperty("idOrdeningreso")
    private Integer idOrdenIngreso;

    @NotNull(message = "El ID Detalle de Orden de Ingreso es obligatorio")
    @JsonProperty("idDetordeningreso")
    private Integer idDetOrdenIngreso;

    @NotNull(message = "El ID Detalle de Orden de Ingreso Peso es obligatorio")
    @JsonProperty("idDetordeningresopeso")
    private Integer idDetOrdenIngresoPeso;

    @NotNull(message = "El Status de Rollo en Orden de Ingreso es obligatorio")
    private Integer statusRolloIngreso;

    @NotNull(message = "El ID de partida es obligatorio")
    private Integer idPartida;

    @NotNull(message = "El ID de detalle de partida es obligatorio")
    @JsonProperty("idDetallePartida")
    private Integer idDetPartida;

    @NotNull(message = "El campo sinCobro de partida es obligatorio")
    private String sinCobro;

    @NotNull(message = "El Status de Rollo en partida es obligatorio")
    @JsonProperty("statusRollPartida")
    private String statusRolloPartida;

    @NotBlank(message = "El código del rollo es obligatorio")
    private String codRollo;

    @NotNull(message = "El peso del rollo es obligatorio")
    @Positive(message = "El peso del rollo debe ser positivo")
    private BigDecimal peso;

    @NotNull(message = "El ID Orden de Ingreso Almacen es obligatorio")
    private Integer idOrdeningresoAlmacen;

    @NotNull(message = "El ID Detalle Orden de Ingreso Almacen es obligatorio")
    private Integer idDetordeningresoAlmacen;

    @NotNull(message = "El ID Detalle Peso Orden de Ingreso Almacen es obligatorio")
    private String idDetordeningresopesoAlmacen;

    @NotNull(message = "El Status de Rollo en almacen es obligatorio")
    private String statusRolloAlmacen;

    private Boolean selected;
}

