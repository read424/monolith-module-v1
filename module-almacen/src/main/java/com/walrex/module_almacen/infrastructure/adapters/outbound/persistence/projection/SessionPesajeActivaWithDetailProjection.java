package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

public interface SessionPesajeActivaWithDetailProjection {
    Integer getId();
    Integer getIdDetOrdenIngreso();
    Integer getIdOrdenIngreso();
    Integer getCntRollos();
    Double getTotalKilos();
    Integer getCntRegistro();
    String getLote();
}
