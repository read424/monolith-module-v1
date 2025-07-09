package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request para registrar devolución de rollos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarDevolucionRollosRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @JsonProperty("id_cliente")
    private Integer idCliente;

    @NotNull(message = "El ID del motivo es obligatorio")
    @JsonProperty("id_motivo")
    private Integer idMotivo;

    @NotNull(message = "La fecha de devolución es obligatoria")
    @JsonProperty("fecha_devolucion")
    private LocalDate fechaDevolucion;

    private String observacion;

    @NotEmpty(message = "La lista de articulos no puede estar vacía")
    @Valid
    private List<ArticuloDevolucionRequest> articulos;
}
