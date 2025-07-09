package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MotivoDevolucion", description = "Modelo de datos para motivos de devolución")
public class MotivoDevolucionResponse {

    @Schema(description = "Identificador único del motivo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Descripción del motivo de devolución", example = "DEFECTO DE PRODUCCIÓN", required = true, maxLength = 255)
    private String descripcion;

    @Schema(description = "Estado del motivo (1=activo, 0=inactivo)", example = "1", defaultValue = "1")
    private Integer status;
}
