package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Proyección para rollos sin levantamiento asignado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnliftedRollProjection {

    /** ID del rollo en crudo (drr.id_detordeningresopeso) */
    private Integer idDetOrdenIngresoPeso;

    /** ID del levantamiento (debería ser NULL para rollos no levantados) */
    private Integer idLevantamiento;

    /** ID del rollo en almacén/guía de ingreso (drr.id_detordeningresopeso_alm) */
    private Integer idDetOrdenIngresoPesoAlm;

    /** Status del rollo en crudo (dp.status) */
    private Integer statusCrudo;

    /** ID del rollo relacionado en almacén (dp2.id_detordeningresopeso) */
    private Integer idDetOrdenIngresoPesoAlmacen;

    /** Status del rollo en almacén (dp2.status) - puede ser NULL */
    private Integer statusAlmacen;

    /**
     * Verifica si el rollo de almacén debe ser deshabilitado
     * @return true si statusAlmacen != null AND statusAlmacen != 10
     */
    public boolean shouldDisableAlmacenRoll() {
        return statusAlmacen != null && statusAlmacen != 10;
    }
}
