package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "ventas", name = "tb_conductor")
public class DriverEntity {
    @Id
    @Column("id_conductor")
    private Long idConductor;

    @Column("id_tipo_doc")
    private Integer idTipoDoc;

    @Column("num_documento")
    private String numDocumento;

    @Column("apellidos")
    private String apellidos;

    @Column("nombres")
    private String nombres;

    @Column("num_licencia")
    private String numLicencia;

    @Column("status")
    private String status;

    @Column("id_user")
    private Integer idUser;

    @Column("create_at")
    private LocalDateTime createAt;

    @Column("update_at")
    private LocalDateTime updateAt;
}