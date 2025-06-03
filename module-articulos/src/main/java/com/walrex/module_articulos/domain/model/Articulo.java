package com.walrex.module_articulos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Articulo {
    private Long idArticulo;
    private Integer idFamilia;
    private Integer idGrupo;
    private String codArticulo;
    private String descArticulo;
    private Integer idMedida;
    private Integer idUnidad;
    private Integer idMarca;
    private String descripcion;
    private Double mtoCompra;
    private LocalDateTime fecIngreso;
    private Integer status;
    private Integer idUnidadConsumo;
    private Integer idMoneda;
    private Boolean isTransformacion;
}
