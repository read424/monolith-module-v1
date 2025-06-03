package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("almacenes.kardex")
public class KardexEntity {
    @Id
    private Long id_kardex;
    @Column("tipo_kardex")
    private Integer tipo_movimiento;
    private String detalle;
    private BigDecimal cantidad;
    @Column("valor_unidad")
    private BigDecimal costo;
    @Column("valor_total")
    private BigDecimal valorTotal;
    private LocalDate fecha_movimiento;
    private Integer id_articulo;
    private Integer status;
    private Integer id_unidad;
    private Integer id_unidad_salida;
    private Integer id_almacen;
    @Column("saldo_stock")
    private BigDecimal saldo_actual;//saldo disponible del articulo en almacen
    private Integer id_documento;
    private Integer id_lote;
    private Integer id_detalle_documento;
    private BigDecimal saldoLote;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
