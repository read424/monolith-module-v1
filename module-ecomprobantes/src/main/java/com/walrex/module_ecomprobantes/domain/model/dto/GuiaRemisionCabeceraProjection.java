package com.walrex.module_ecomprobantes.domain.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

/**
 * Proyección para la cabecera de guía de remisión.
 * Contiene todos los datos principales del comprobante y entidades
 * relacionadas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionCabeceraProjection {

    // Datos del comprobante
    private Integer idComprobante;
    private String nuCompro;
    private String nuSerie;
    private String nroComprobante;
    private LocalDate feEmision;

    // Datos de la empresa
    private String numDocumento;
    private String razonSocial;
    private String companyDireccion;
    private String tlfPrincipal;
    private String numFax;
    private String tlfSecundario;

    // Datos del cliente
    private String codTipodoc;
    private String abrevDoc;
    private String nuRuc;
    private String rznSocial;
    private String direccion;

    // Datos del motivo de traslado
    private String codMotivoTraslado;
    private String descMotivoTraslado;

    // Datos de modalidad de traslado
    private String codModTraslado;
    private String descModTraslado;

    // Datos del transportista
    private String docTranspCodTipodoc;
    private String abrevDocTransp;
    private String transpNumTipoDocumento;
    private String transpRazonSocial;

    // Datos del conductor
    private String codTipodocConductor;
    private String tipoDocConductor;
    private String abrevDocConductor;
    private String numDocumentoConductor;
    private String nombresConductor;
    private String apellidosConductor;
    private String numLicenciaConductor;

    // Datos de direcciones
    private String direcLlegada;
    private String ubigeoLlegada;
    private String direcPartida;
    private String ubigeoPartida;

    // Datos del vehiculo
    private String numPlaca;

    // Lista de detalles
    private List<GuiaRemisionDetalleProjection> detalles;
}