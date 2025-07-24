package com.walrex.module_ecomprobantes.domain.model.dto;

import java.time.LocalDate;

import com.walrex.module_ecomprobantes.domain.model.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para la solicitud de generación de guía de remisión.
 * Contiene todos los datos necesarios para generar el documento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionRequestDTO {

    @NotNull(message = "El ID de la orden de salida es obligatorio")
    private Integer idOrdenSalida;

    @NotNull(message = "El indicador de guía SUNAT es obligatorio")
    private Boolean isGuiaSunat;

    @NotNull(message = "El motivo de traslado es obligatorio")
    private Integer idMotivoTraslado;

    @NotNull(message = "La modalidad de traslado es obligatoria")
    private Integer idModalidad;

    @NotNull(message = "La empresa de transporte es obligatoria")
    private Integer idEmpresaTrans;

    @NotNull(message = "El conductor es obligatorio")
    private Integer idConductor;

    @NotNull(message = "El número de placa es obligatorio")
    private String numPlaca;

    @NotNull(message = "El punto de llegada es obligatorio")
    private Integer idLlegada;

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;

    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private ClientModel client;

    @Valid
    @NotNull(message = "Los datos de la empresa son obligatorios")
    private CompanyModel company;

    @Valid
    @NotNull(message = "Los datos del transportista son obligatorios")
    private CarrierModel carrier;

    @Valid
    @NotNull(message = "Los datos del conductor son obligatorios")
    private DriverPersonModel driver;

    @Valid
    @NotNull(message = "Los datos del vehículo son obligatorios")
    private VehicleModel vehicle;

    @Valid
    @NotNull(message = "Los datos del envío son obligatorios")
    private ShipmentModel shipment;

    @Valid
    @NotNull(message = "Los datos del despacho son obligatorios")
    private DespatchModel despatch;
}