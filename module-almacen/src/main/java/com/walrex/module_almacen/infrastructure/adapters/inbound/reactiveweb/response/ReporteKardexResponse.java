package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReporteKardexResponse {
    private List<KardexArticuloDTO> data;
    private boolean success;
    private String mensaje;
}
