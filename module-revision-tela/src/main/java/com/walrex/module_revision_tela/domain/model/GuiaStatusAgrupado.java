package com.walrex.module_revision_tela.domain.model;

import java.time.LocalDate;
import java.util.Map;

import lombok.*;

/**
 * Modelo de dominio que representa una guía de ingreso con sus status agrupados
 * Contiene el mapa de status calculados a partir de los rollos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaStatusAgrupado {

    private Integer idOrdeningreso;
    private LocalDate fecIngreso;
    private Integer idCliente;
    private Integer idComprobante;
    private String nuSerie;
    private String nuComprobante;
    private Integer statusOrden;

    /**
     * Mapa donde:
     * - key: status calculado (1, 3 o 10)
     * - value: status_orden de la guía
     */
    private Map<Integer, Integer> statusMap;
}
