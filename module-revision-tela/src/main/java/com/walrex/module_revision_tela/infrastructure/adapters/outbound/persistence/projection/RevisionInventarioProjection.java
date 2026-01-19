package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDate;

import org.springframework.data.relational.core.mapping.Column;

import lombok.*;

/**
 * Proyección para la consulta compleja de revisión de inventario
 * Incluye información de orden de ingreso, detalle, rollos, partidas y status
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionInventarioProjection {

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("fec_ingreso")
    private LocalDate fecIngreso;

    @Column("nu_serie")
    private String nuSerie;

    @Column("nu_comprobante")
    private String nuComprobante;

    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_articulo")
    private Integer idArticulo;

    @Column("id_detordeningresopeso")
    private Integer idDetordeningresopeso;

    @Column("id_partida")
    private Integer idPartida;

    @Column("status")
    private Integer status;

    @Column("as_crudo")
    private Integer asCrudo;

    @Column("id_detordeningresopeso_alm")
    private Integer idDetordeningresopesoAlm;

    @Column("status_almacen")
    private Integer statusAlmacen;

    @Column("id_almacen")
    private Integer idAlmacen;
}
