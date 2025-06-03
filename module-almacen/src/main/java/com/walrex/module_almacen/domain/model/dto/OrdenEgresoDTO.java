package com.walrex.module_almacen.domain.model.dto;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Motivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEgresoDTO {
    private Long id;
    private Motivo motivo;
    private Integer isInterno;
    private Integer idTipoComprobante;
    private String numComprobante;
    private Almacen almacenOrigen;
    private Almacen almacenDestino;
    private LocalDate fecRegistro;
    private Integer idUsuario;
    private Date fecEntrega;
    private Integer idUsuarioEntrega;
    private Integer entregado;
    private Integer idDocumentoRef;
    private String codEgreso;
    private Integer status;
    private Integer idCliente;
    private Integer idRequerimiento;
    private Integer idSupervisor;
    private String observacion;
    private Integer correlativoMotivo;
    private List<DetalleEgresoDTO> detalles;
}
