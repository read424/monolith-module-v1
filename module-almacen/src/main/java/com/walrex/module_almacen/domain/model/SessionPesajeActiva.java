package com.walrex.module_almacen.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionPesajeActiva {
    private Integer id;
    private Integer idDetOrdenIngreso;
    private Integer cntRollos;
    private Double totKg;
    private Integer cntRegistro;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String status;
}
