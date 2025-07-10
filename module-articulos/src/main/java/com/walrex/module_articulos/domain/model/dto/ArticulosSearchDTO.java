package com.walrex.module_articulos.domain.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ArticulosSearchDTO extends ArticuloDto {
}
