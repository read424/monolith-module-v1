package com.walrex.module_almacen.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloPesajeSession {
    /** id de session_pesaje_activa; null si aún no existe la sesión */
    private Integer id;
    private Integer idDetOrdenIngreso;
    private Integer nuRollos;
    private BigDecimal pesoRef;
    private Integer cntRollSaved;
    private BigDecimal totalSaved;
    private Integer lastRollSaved;
    private String status;
}
