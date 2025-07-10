package com.walrex.module_almacen.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para representar el listado de órdenes de salida por devolución.
 * Contiene la información consolidada de la orden de salida con datos del cliente y comprobante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenSalidaDevolucionDTO {
    
    private Long idOrdenSalida;
    private String codigoSalida;
    private LocalDate fechaEntrega;
    private Long idCliente;
    private String razonSocial;
    private Long idComprobante;
    private String numeroSerie;
    private String numeroComprobante;
} 