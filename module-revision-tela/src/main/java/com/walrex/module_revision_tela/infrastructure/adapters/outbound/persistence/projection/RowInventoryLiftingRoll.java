package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowInventoryLiftingRoll {

    private Integer id;

    private Integer id_ordeningreso;

    private Integer id_detordeningreso;

    private Integer id_detordeningresopeso;

    private Integer id_partida;

    private Integer as_crudo;

    private Integer id_levantamiento;
}
