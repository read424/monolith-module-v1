package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDate;

import org.springframework.data.relational.core.mapping.Column;

import lombok.*;

/**
 * Proyección para la consulta de status de guías según status de rollos
 * Basada en el SQL con JOINs entre ordeningreso, detordeningreso y detordeningresopeso
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaStatusProjection {

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("fec_ingreso")
    private LocalDate fecIngreso;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("id_comprobante")
    private Integer idComprobante;

    @Column("nu_serie")
    private String nuSerie;

    @Column("nu_comprobante")
    private String nuComprobante;

    @Column("status_orden")
    private Integer statusOrden;

    @Column("status")
    private Integer status;
}
