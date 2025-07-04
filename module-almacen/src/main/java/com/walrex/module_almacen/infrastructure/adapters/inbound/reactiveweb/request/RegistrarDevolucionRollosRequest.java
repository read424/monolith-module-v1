package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.util.List;

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

    @NotNull(message = "El ID del motivo es obligatorio")
    private Integer idMotivo;

    @NotNull(message = "El ID del almacén destino es obligatorio")
    private Integer idAlmacenDestino;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idCliente;

    private String observacion;

    @NotEmpty(message = "La lista de articulos no puede estar vacía")
    @Valid
    private List<ArticuloDevolucionRequest> articulos;
}