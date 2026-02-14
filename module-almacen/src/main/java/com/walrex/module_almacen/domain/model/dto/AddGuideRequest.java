package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGuideRequest {
    private String nu_serie;
    private String fec_registro;
    private String nu_comprobante;
    private Integer id_comprobante;
    private Integer id_cliente;
    private List<AddGuideDetail> detalles;
}
