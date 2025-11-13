package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrdenIngresoLogisticaRequestDto {

    @Valid
    private AlmacenTipoIngresoLogisticaRequestDto id_tipo_almacen;

    @Valid
    private MotivoIngresoLogisticaRequestDto motivo;

    private Integer id_orden;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDateTime fec_ingreso;

    @NotNull(message = "Tipo de comprobante es obligatorio")
    private Integer id_compro;

    @NotBlank(message = "Codigo de serie documento es obligatorio")
    private String nu_serie;

    @NotBlank(message = "Numero de documento es obligatorio")
    private String nu_comprobante;

    private LocalDateTime fec_ref;

    @JsonProperty("comprobante_ref")
    private String comprobanteRef;

    @NotNull(message = "Cliente/Proveedor es obligatorio")
    private Integer id_cliente;

    private String observacion;

    @Valid
    private List<ItemArticuloLogisticaRequestDto> detalles;
}
