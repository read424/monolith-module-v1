package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.orden_ingreso_documentos")
public class OrdenIngresoDocumentoEntity {
    @Id
    private Integer id;

    @Column("id_ordeningreso")
    private Integer idOrdenIngreso;

    @Column("id_tipo_documento")
    private Integer idTipoDocumento;

    @Column("id_documento")
    private Integer idDocumento;

    @Column("id_almacen")
    private Integer idAlmacen;
}
