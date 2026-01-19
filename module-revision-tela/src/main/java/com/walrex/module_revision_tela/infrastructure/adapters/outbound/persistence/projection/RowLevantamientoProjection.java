package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowLevantamientoProjection {

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_detordeningreso")
    private Integer idDetOrdenIngreso;

    @Column("id_partida")
    private Integer idPartida;

    @Column("nu_comprobante")
    private String numComprobante;

    @Column("nu_serie")
    private String numSerie;

    @Column("fec_ingreso")
    private LocalDate fecIngreso;

    @Column("no_alias")
    private String noAlias;

    @Column("lote")
    private String numLote;

    @Column("desc_articulo")
    private String descArticulo;

    @Column("id_levantamiento")
    private Integer idLevantamiento;

    @Column("rollos_partida")
    private Integer cntRolls;

    @Column("cnt_lev")
    private Integer cntLevantado;

    @Column("total_rollos")
    private Integer totalRolls;

    @Column("nu_ruc")
    private String numRuc;
}
