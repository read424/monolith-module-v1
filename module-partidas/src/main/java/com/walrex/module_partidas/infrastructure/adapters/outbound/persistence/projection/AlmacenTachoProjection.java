package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import lombok.*;

/**
 * Proyección para la consulta compleja de Almacen Tacho
 * Basada en el SQL con múltiples JOINs y campos calculados
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenTachoProjection {

    @Id
    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("razon_social")
    private String razonSocial;

    @Column("no_alias")
    private String noAlias;

    @Column("fec_registro")
    private LocalDateTime fecRegistro;

    @Column("cod_ingreso")
    private String codIngreso;

    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_partida")
    private Integer idPartida;

    @Column("cod_partida")
    private String codPartida;

    @Column("cnt_rollos")
    private Integer cntRollos;

    @Column("cod_receta")
    private String codReceta;

    @Column("no_colores")
    private String noColores;

    @Column("id_tipo_tenido")
    private Integer idTipoTenido;

    @Column("desc_tenido")
    private String descTenido;

    @Column("no_gama")
    private String noGama;
}
