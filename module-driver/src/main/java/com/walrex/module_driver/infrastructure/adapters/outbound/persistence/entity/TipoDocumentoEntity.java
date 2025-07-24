package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "rrhh", name = "tctipo_doc")
public class TipoDocumentoEntity {
    @Id
    @Column("id_tipodoc")
    private Integer idTipoDocumento;

    @Column("no_tipodoc")
    private String descTipoDocumento;

    @Column("abrev_doc")
    private String abrevDoc;

    @Column("predeterminado")
    private String predeterminado;

    @Column("status")
    private String status;

    @Column("cod_tipodoc")
    private Integer codTipoDocumento;

    @Column("is_sunat")
    private Integer isSunat;

}
