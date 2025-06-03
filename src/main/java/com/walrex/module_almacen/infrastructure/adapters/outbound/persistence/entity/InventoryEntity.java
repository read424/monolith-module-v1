package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.inventario")
public class InventoryEntity {
    @Column("id_articulo")
    private Integer idArticulo;
    private BigDecimal stock;
    @Column("varios_lotes")
    private Integer variosLotes;
    @Column("id_almacen")
    private Integer idAlmacen;
}