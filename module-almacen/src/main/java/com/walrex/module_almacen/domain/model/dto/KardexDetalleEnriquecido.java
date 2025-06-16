package com.walrex.module_almacen.domain.model.dto;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.KardexDetalleProjection;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KardexDetalleEnriquecido extends KardexDetalleProjection {
    private String codigoDocumento;
}
