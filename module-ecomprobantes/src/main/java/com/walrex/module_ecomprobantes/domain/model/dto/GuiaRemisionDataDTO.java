package com.walrex.module_ecomprobantes.domain.model.dto;

import java.time.LocalDate;
import java.util.List;

import com.walrex.module_ecomprobantes.domain.model.*;

import lombok.*;

/**
 * DTO que contiene todos los datos necesarios para generar una guía de
 * remisión.
 * Agrupa todos los modelos de dominio en una sola estructura.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionDataDTO {

    // Datos básicos de la guía
    private Integer idOrdenSalida;
    private Boolean isGuiaSunat;
    private Integer idMotivoTraslado;
    private String descMotivoTraslado;
    private Integer idModalidad;
    private String descModalidad;
    private Integer idEmpresaTrans;
    private String descEmpresaTrans;
    private Integer idConductor;
    private String numPlaca;
    private Integer idLlegada;
    private String descLlegada;
    private LocalDate fechaEntrega;
    private LocalDate fechaEmision;

    // Datos de las entidades
    private ClientModel client;
    private CompanyModel company;
    private CarrierModel carrier;
    private DriverPersonModel driver;
    private VehicleModel vehicle;
    private ShipmentModel shipment;
    private DespatchModel despatch;

    // Datos adicionales
    private String numeroGuia;
    private String serieGuia;
    private String correlativoGuia;
    private String observaciones;

    // Lista de detalles del despacho
    private List<DespatchDetailModel> detalles;

    // Datos de direcciones
    private AddressModel direccionOrigen;
    private AddressModel direccionDestino;
    private AddressModel direccionTransportista;
}