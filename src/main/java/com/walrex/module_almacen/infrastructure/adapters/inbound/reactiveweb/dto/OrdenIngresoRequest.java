package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdenIngresoRequest {

    private Integer idOrdenIngreso;

    @NotNull(message = "El almacén es obligatorio")
    @Valid
    private AlmacenDto almacen;

    @NotNull(message = "El motivo es obligatorio")
    private Integer motivo;

    private Integer idOrdenCompra;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecIngreso;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private Integer comprobante;

    @NotNull(message = "La serie es obligatoria")
    private String codSerie;

    @NotNull(message = "El número de comprobante es obligatorio")
    private String nroComprobante;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecComprobante;

    private String comprobanteRef;
    private Integer idOrigen;

    @NotNull(message = "El cliente es obligatorio")
    private Integer idCliente;

    @NotEmpty(message = "Debe incluir al menos un detalle")
    @Valid
    private List<DetalleOrdenIngresoDto> detalles;
}
