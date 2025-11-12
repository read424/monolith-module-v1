package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO para consultar rollos disponibles para devolución
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarRollosDisponiblesRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser positivo")
    @JsonProperty("id_cliente")
    private Integer idCliente;

    @NotNull(message = "El ID del artículo es obligatorio")
    @Positive(message = "El ID del artículo debe ser positivo")
    @JsonProperty("id_articulo")
    private Integer idArticulo;
}
