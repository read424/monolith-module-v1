package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DocumentoMovimientoEgresoKardex {
    private Integer id_ordensalida;
    @JsonProperty("cod_salida")
    private String cod_egreso;
    @JsonProperty("fec_enrega")
    private LocalDate fec_egreso;
    private String no_motivo;
}
