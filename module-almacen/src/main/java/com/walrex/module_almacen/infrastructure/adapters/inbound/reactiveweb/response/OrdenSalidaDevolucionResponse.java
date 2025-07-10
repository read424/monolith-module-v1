package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response para el listado de órdenes de salida por devolución.
 * Contiene la información consolidada de la orden de salida con datos del
 * cliente y comprobante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response con información de órdenes de salida por devolución")
public class OrdenSalidaDevolucionResponse {

    @Schema(description = "ID único de la orden de salida", example = "1001")
    private Long idOrdenSalida;

    @Schema(description = "Código de la orden de salida", example = "SAL-2024-001")
    private String codigoSalida;

    @Schema(description = "Fecha de registro de la orden", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;

    @Schema(description = "Fecha de entrega de la orden", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEntrega;

    @Schema(description = "ID del cliente", example = "501")
    private Long idCliente;

    @Schema(description = "Razón social del cliente", example = "Juan Pérez Gómez")
    private String razonSocial;

    @Schema(description = "ID del comprobante", example = "1234")
    private Long idComprobante;

    @Schema(description = "Número de serie del comprobante", example = "001")
    private String numeroSerie;

    @Schema(description = "Número del comprobante", example = "GU-2024-001")
    private String numeroComprobante;
}