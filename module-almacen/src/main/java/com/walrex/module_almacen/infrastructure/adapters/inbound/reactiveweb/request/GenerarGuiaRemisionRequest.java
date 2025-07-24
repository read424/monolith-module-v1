package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarGuiaRemisionRequest {

    @JsonProperty("guia_sunat")
    private Boolean isGuiaSunat;

    @NotNull(message = "ID de orden de salida es requerido")
    @JsonProperty("id_orden_salida")
    private Long idOrdenSalida;

    @JsonProperty("motivo_traslado")
    private Integer idMotivoTraslado;

    @NotNull(message = "Modalidad Transporte es requerida")
    @JsonProperty("id_modalidad")
    private Integer idModalidad;

    @NotNull(message = "Tipo de empresa transportista es requerido")
    @JsonProperty("id_empresa_transp")
    private Integer idEmpresaTransp;

    @NotNull(message = "Conductor es requerido")
    @JsonProperty("id_conductor")
    private Integer idConductor;

    @NotNull(message = "Número de placa es requerido")
    @JsonProperty("num_placa")
    private String numPlaca;

    @NotNull(message = "Llegada es requerida")
    @JsonProperty("id_direc_entrega")
    private Integer idLlegada;

    @JsonProperty("fecha_entrega")
    private LocalDate fechaEntrega;
}
