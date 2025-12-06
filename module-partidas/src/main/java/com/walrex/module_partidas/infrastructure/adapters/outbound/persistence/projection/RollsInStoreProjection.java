package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RollsInStoreProjection {

    private Integer idDetOrdenIngresoPeso;

    private Integer idOrdenIngreso;

    private Integer idDetOrdenIngreso;

    private Integer idRolloIngreso;

    private String codigo;

    private Double peso;
}
