package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CrearMotivoDevolucionRequest", description = "Request para crear un nuevo motivo de devolución")
public class CrearMotivoDevolucionRequest {

    @NotNull(message = "Campo descripción es obligatorio")
    @Schema(
        description = "Descripción del motivo de devolución", 
        example = "DEFECTO DE FABRICACIÓN", 
        required = true,
        maxLength = 255
    )
    private String descripcion;
}
