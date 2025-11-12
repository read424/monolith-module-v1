package com.walrex.module_almacen.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = KardexReporte.KardexReporteBuilder.class)
public class KardexReporte implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<KardexArticuloDTO> articulos;
    private Integer totalArticulos;
    private LocalDateTime fechaGeneracion;

    @JsonPOJOBuilder(withPrefix = "")
    public static class KardexReporteBuilder {
        // Lombok genera este builder automáticamente
    }
}
