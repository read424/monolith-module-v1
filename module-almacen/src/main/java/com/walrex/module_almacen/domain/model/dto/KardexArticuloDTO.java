package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = KardexArticuloDTO.KardexArticuloDTOBuilder.class)
public class KardexArticuloDTO {
    private static final long serialVersionUID = 1L;

    private Integer idArticulo;
    private String codArticulo;
    private String descArticulo;
    private BigDecimal precioAvg;
    private BigDecimal totalValorizado;
    private BigDecimal stockDisponible;
    private List<KardexDetalleDTO> detalles;

    @JsonPOJOBuilder(withPrefix = "")
    public static class KardexArticuloDTOBuilder {
        // Lombok genera este builder automáticamente
    }
}
