package com.walrex.module_almacen.domain.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuideArticleRequest {
    @NotNull(message = "El id_articulo es obligatorio")
    @Positive(message = "El id_articulo debe ser mayor a cero")
    private Integer id_articulo;

    @NotNull(message = "El peso_ref es obligatorio")
    @Positive(message = "El peso_ref debe ser mayor a cero")
    private Double peso_ref;

    @NotNull(message = "El nu_rollos es obligatorio")
    @Min(value = 1, message = "El nu_rollos debe ser al menos 1")
    private Integer nu_rollos;
}
