package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemArticuloRequest {
    @JsonProperty("id_articulo")
    @NotNull(message = "Campo id_articulo es obligatorio")
    private Integer id;
    @JsonProperty("cod_articulo")
    @NotBlank(message = "Campo cod_articulo no puede ser vacio")
    private String codigo;
    @JsonProperty("desc_articulo")
    @NotBlank(message = "Campo desc_articulo no puede ser vacio")
    private String descripcion;
    @JsonProperty("id_unidad")
    @NotNull(message = "Campo id_unidad es obligatorio")
    private Integer idUnidad;
    @JsonProperty("abrev_unidad")
    @NotBlank(message = "Campo abrev_unidad es obligatorio")
    private String abrevUnidad;
    @NotNull(message = "Campo cantidad es obligatorio")
    private Double cantidad;
    @JsonProperty("id_unidad_consumo")
    @NotNull(message = "Campo id_unidad_consumo es obligatorio")
    private Integer idUnidadConsumo;
}
