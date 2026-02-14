package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import java.time.LocalDate;

public interface GuidePendingProjection {
    Integer getId_ordeningreso();
    LocalDate getFec_registro();
    String getNu_serie();
    String getNu_comprobante();
    String getRazon_social();
    Integer getId_detordeningreso();
    String getLote();
    Integer getId_articulo();
    String getCod_articulo();
    String getDesc_articulo();
    Integer getTotal_rollos();
    Integer getNum_rollo();
    Integer getRolls_saved();
}
