package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidePendingDetail {
    private Integer id_detordeningreso;
    private String lote;
    private Integer id_articulo;
    private String cod_articulo;
    private String desc_articulo;
    private Integer total_rollos;
    private Integer num_rollo;
    private Integer rolls_saved;
    @JsonProperty("peso_ref")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal peso_ref;
}
