package com.walrex.module_ecomprobantes.domain.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que representa la tabla tipo_serie del esquema facturacion.
 * 
 * Esta entidad maneja la configuración de series para diferentes tipos de
 * comprobantes.
 * Cada serie está asociada a un tipo de comprobante específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipo_serie", schema = "facturacion")
public class TipoSerieEntity {

    @Id
    @Column("id_serie")
    private Integer idSerie;

    @Column("nu_serie")
    private String nuSerie;

    @Column("il_estado")
    private Boolean ilEstado;

    @Column("id_compro")
    private Integer idCompro;

    @Column("nu_compro")
    private Integer nuCompro;

    @Column("is_cpe")
    private Integer isCpe;
}