package com.walrex.module_almacen.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesajeDetalle {
    private Integer id_detordeningresopeso;
    private Integer idOrdenIngreso;
    private Double peso_rollo;
    private String cod_rollo;
    private Integer cnt_registrados;
    private Integer cnt_rollos;
    private String lote;
    private Boolean completado;

    // Helper fields for service logic (not exposed in domain typically, but useful here)
    private Integer id_detordeningreso;
    private Integer id_session_hidden;
}
