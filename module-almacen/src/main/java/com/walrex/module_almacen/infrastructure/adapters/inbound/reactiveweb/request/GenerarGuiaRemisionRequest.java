package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarGuiaRemisionRequest {

    @NotNull(message = "ID de orden de salida es requerido")
    private Long idOrdenSalida;

    @NotNull(message = "Tipo de empresa transportista es requerido")
    private Integer idEmpresaTransp;

    @NotNull(message = "Modalidad es requerida")
    private Integer idModalidad;

    @NotNull(message = "Tipo de documento del chofer es requerido")
    private Integer idTipDocChofer;

    @NotNull(message = "Número de documento del chofer es requerido")
    private String numDocChofer;

    @NotNull(message = "Número de placa es requerido")
    private String numPlaca;

    @NotNull(message = "Llegada es requerida")
    private Integer idLlegada;
}