package com.walrex.module_partidas.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoDocumentoDTO {
    private Integer id;
    private Integer id_ordeningreso;
    private Integer id_tipo_documento;
    private Integer id_documento;
    private Integer id_almacen;
}
