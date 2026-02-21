package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidePendingResponse {
    private Integer id_ordeningreso;
    private LocalDate fec_registro;
    private String nu_serie;
    private String nu_comprobante;
    private String razon_social;
    private List<GuidePendingDetail> details;
}
