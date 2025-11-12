package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemArticuloLogisticaRequestDto {
    @JsonProperty("id_articulo")
    private Integer idArticulo;
    @JsonProperty("id_detordencompra")
    private Integer idDetOrdenCompra;
    @NotNull
    private Integer id_tipo_producto;
    @NotNull
    private Integer id_tipo_producto_fa;
    @NotBlank
    private String nu_lote;
    @NotNull
    private BigDecimal cantidad;
    @NotNull
    @JsonProperty("id_unidad")
    private Integer idUnidad;
    private Boolean exento_imp;
    @JsonProperty("id_moneda")
    private Integer idMoneda;
    @JsonProperty("id_unidad_consumo")
    private Integer idUnidadConsumo;
    private String isMultiplo;
    @JsonProperty("valor_conv")
    private Integer valorConversion;
    @JsonProperty("costo")
    private BigDecimal mto_compra;
    private BigDecimal mto_igv;
    private BigDecimal subtotal;
    @JsonProperty("cnt_saldo")
    private BigDecimal cantidadSaldo;
    private BigDecimal total_igv;
    private BigDecimal total;
    @JsonProperty("subGridOptions")
    private ItemRolloRequestDto detailsRolls[];
}
