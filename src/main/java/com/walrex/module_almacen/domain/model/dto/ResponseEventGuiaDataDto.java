package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEventGuiaDataDto {
    private Integer idOrdenSalida;
    private Integer idComprobante;
    private String codigoComprobante;
}
