package com.walrex.module_almacen.domain.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

/**
 * DTO del dominio para devolución de rollos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalidaDevolucionDTO {
    private Long idOrdenSalida;
    private Integer idMotivo;
    private Integer idMotivoDevolucion;
    private Integer idAlmacenOrigen;
    private String codSalida;
    private Integer idAlmacenDestino;
    private Integer idCliente;
    private String observacion;
    private LocalDate fechaRegistro;
    private Integer idUsuario;
    private List<DevolucionArticuloDTO> articulos;
}
