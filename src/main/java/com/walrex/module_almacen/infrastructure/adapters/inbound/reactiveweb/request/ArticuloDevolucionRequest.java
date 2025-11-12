package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloDevolucionRequest {

    @NotNull(message = "El ID del articulo es obligatorio")
    @JsonProperty("id_articulo")
    private Integer idArticulo;

    @NotNull(message = "Cantidad es obligatorio")
    private Integer cantidad;

    @NotNull(message = "El total de peso es obligatorio")
    @JsonProperty("total_peso")
    private Double totalPeso;

    @NotEmpty(message = "La lista de rollos es obligatoria")
    private List<RolloDevolucionRequest> rollos;
}
