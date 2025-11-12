package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = KardexDetalleDTO.KardexDetalleDTOBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KardexDetalleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id_orden_documento;
    private Integer type_kardex;
    private String descripcion;
    private String detail_document_ingreso;
    private String detail_document_egreso;
    private BigDecimal cantidad;
    private BigDecimal precio_compra;
    private BigDecimal total_compra;
    private BigDecimal stock_actual;
    private BigDecimal stock_lote;
    private Integer id_unidad;
    private String desc_unidad;
    private Integer id_unidad_salida;
    private String desc_unidad_salida;
    private LocalDate fec_movimiento;

    @JsonPOJOBuilder(withPrefix = "")
    public static class KardexDetalleDTOBuilder {
        // Lombok genera este builder automáticamente
    }
}
