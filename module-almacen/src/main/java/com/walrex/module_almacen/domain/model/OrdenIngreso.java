package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrdenIngreso {
    private Integer id;
    private String cod_ingreso;
    private Almacen almacen;
    private Motivo motivo;
    private Integer idOrdenCompra;
    private LocalDate fechaIngreso;
    private Integer comprobante;
    private String codSerie;
    private String nroComprobante;
    private LocalDate fechaComprobante;
    private String comprobanteRef;
    private Integer idOrigen;
    private Integer idCliente;
    private Integer idOrdServ;
    private String observacion;
    private List<DetalleOrdenIngreso> detalles;
}
