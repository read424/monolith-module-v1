package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import java.math.BigDecimal;

public interface ArticuloPesajeSessionProjection {
    /** id de session_pesaje_activa (null si no existe sesión). */
    Integer getId();
    Integer getIdDetOrdenIngreso();
    Integer getNuRollos();
    BigDecimal getPesoRef();
    Integer getCntRollSaved();
    BigDecimal getTotalKgSaved();
    Integer getLastRollSaved();
    String getStatus();
}
