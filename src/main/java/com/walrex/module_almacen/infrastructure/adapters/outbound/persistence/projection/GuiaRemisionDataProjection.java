package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionDataProjection {
    private Integer idCliente;
    private LocalDate fechaEmision; // fec_entrega
    private Integer idMotivo;
    private Integer tipoComprobante; // null por defecto
    private Integer tipoSerie; // null por defecto
}
