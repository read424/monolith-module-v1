package com.walrex.module_almacen.domain.model;

import java.time.LocalDate;

import lombok.*;

/**
 * DTO para representar el listado de órdenes de salida por devolución.
 * Contiene la información consolidada de la orden de salida con datos del
 * cliente y comprobante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenSalidaDevolucionDTO {

    private Long idOrdenSalida;
    private String codigoSalida;
    private LocalDate fechaRegistro;
    private LocalDate fechaEntrega;
    private Long idCliente;
    private String razonSocial;
    private Long idComprobante;
    private String numeroSerie;
    private String numeroComprobante;
    private Integer idTipoSerie;
    private LocalDate fecComunicacion;
}