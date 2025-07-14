package com.walrex.module_ecomprobantes.domain.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.*;

/**
 * DTO para transferencia de datos de comprobantes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDTO {

    private Long idComprobante;
    private Integer idTipoComprobante;
    private Integer tipoSerie;
    private Integer numeroComprobante;
    private Integer idCliente;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDate fechaRegistro;
    private Integer idTipoMoneda;
    private Integer idPago;
    private Integer idFormaPago;
    private Integer idTipoRetencion;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private Integer idMotivo;
    private String observacion;
    private Integer idModalidad;

    @Builder.Default
    private Integer status = 1;

    private Integer codigoResponseSunat;
    private String responseSunat;
    private LocalDate fechaComunicacion;
    private String descripcionMotivo;

    @Builder.Default
    private Short aplicaDetraccion = 0;

    @Builder.Default
    private Short isInafecta = 0;

    @Builder.Default
    private Short isDocumentoAutorizado = 1;

    private String notesSunat;

    private List<DetalleComprobanteDTO> detalles;

}