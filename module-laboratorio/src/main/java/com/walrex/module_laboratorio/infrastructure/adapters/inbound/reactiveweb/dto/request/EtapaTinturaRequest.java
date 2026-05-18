package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtapaTinturaRequest {
    @NotNull(message = "El id_proceso es obligatorio")
    private Integer id_proceso;
    @NotBlank(message = "La descripción de la etapa es obligatoria")
    private String desc_tintura;
    private String observacion;
    private Integer id_usuario;
}
