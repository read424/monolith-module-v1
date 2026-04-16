package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("laboratorio.tb_producto_evento")
public class ProductoEventoEntity {
    @Id
    @Column("id_producto_evento")
    private Integer idProductoEvento;

    @Column("nombre")
    private String nombre;

    @Column("status")
    private Integer status;
}
