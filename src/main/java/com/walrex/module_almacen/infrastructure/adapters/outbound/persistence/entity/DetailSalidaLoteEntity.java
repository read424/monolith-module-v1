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
@Table("almacenes.detalle_salida_lote")
public class DetailSalidaLoteEntity {
    @Id
    private Long id_salida_lote;
    private Integer id_detalle_orden;
    private Integer id_lote;
    private Double cantidad;
    private Double monto_consumo;
    private Double total_monto;
    private Integer id_ordensalida;
}
