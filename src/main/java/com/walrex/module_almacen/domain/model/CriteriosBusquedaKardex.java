package com.walrex.module_almacen.domain.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CriteriosBusquedaKardex {
    private Integer idArticulo;
    private Integer idAlmacen;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
