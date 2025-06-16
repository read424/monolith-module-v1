package com.walrex.module_almacen.domain.model;

import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class KardexReporte {
    private List<KardexArticuloDTO> articulos;
    private Integer totalArticulos;
    private LocalDateTime fechaGeneracion;
}
