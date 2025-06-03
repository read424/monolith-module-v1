package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("almacenes.detalle_ordensalida")
public class DetailSalidaEntity {
    @Id
    private Long id_detalle_orden;
    private Long id_ordensalida;
    private Integer id_articulo;
    private Integer id_unidad;
    private Double cantidad;
    private Integer entregado;
    private Integer id_lote;
    private Double tot_kilos;
    private Double total_kg_sal;
    private Double tot_monto;
    private Double precio;
    private Long id_kardex;
    private Integer status;
}
