package com.walrex.module_partidas.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRolloProcess {

    /**
     * Código del rollo
    */
    @NotNull(message = "El código del rollo es obligatorio")
    @JsonProperty("cod_rollo")
    private String codRollo;

    /**
     * Peso del rollo
    */
    @Positive(message = "El peso del rollo es obligatorio")
    @JsonProperty("peso_acabado")
    private Double pesoRollo;

    /**
     * Id Orden Ingreso
    */
    @NotNull(message = "El id orden ingreso es obligatorio")
    @JsonProperty("id_ordeningreso")
    private Integer idOrdenIngreso;

    /**
     * Id Orden Ingreso Peso
    */
    @NotNull(message = "El id ingreso peso es obligatorio")
    @JsonProperty("id_ingresopeso")
    private Integer idIngresoPeso;

    /**
     * Id Orden Ingreso Almacen Produccion
    */
    @NotNull(message = "El id ingreso almacen es obligatorio")
    @JsonProperty("id_ingreso_almacen")
    private Integer idIngresoAlmacen;

    /**
     * Id Detalle Orden Ingreso Almacen Produccion
    */
    @NotNull(message = "El id ingreso peso almacen es obligatorio")
    @JsonProperty("id_rollo_ingreso")
    private Integer idRolloIngreso;

    /**
     * Id Detalle Partida
    */
    @NotNull(message = "El id detalle partida es obligatorio")
    @JsonProperty("id_detpartida")
    private Integer idDetPartida;

    /**
     * Id Almacen
    */
    @NotNull(message = "El id almacen es obligatorio")
    @JsonProperty("id_almacen")
    private Integer idAlmacen;

    /**
     * Selected
    */
    @NotNull(message = "El selected es obligatorio")
    private Boolean selected;

    /**
     * Estado del rollo
    */
    @NotNull(message = "El status es obligatorio")
    private Integer status;

    /**
     * Delete
    */
    @NotNull(message = "El delete es obligatorio")
    private Integer delete;
}
