package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

public interface SessionPesajeActivaWithDetailProjection {
    Integer getId();
    Integer getId_detordeningreso();
    Integer getCnt_rollos();
    Integer getCnt_registro();
    Integer getId_ordeningreso();
    String getLote();
}
