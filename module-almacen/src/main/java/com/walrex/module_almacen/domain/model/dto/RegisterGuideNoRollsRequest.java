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
public class RegisterGuideNoRollsRequest {
    private String request_id;
    private Integer id_cliente;
    private String nu_serie;
    private String nu_comprobante;
    private String fec_ingreso;
    private List<RegisterGuideNoRollsDetail> details;
}
