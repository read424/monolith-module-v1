package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailShipmentDTO {
    private String codProducto;
    private String descProducto;
    private String unidadMedida;
    private Integer cantidad;
    private Double peso;
}
