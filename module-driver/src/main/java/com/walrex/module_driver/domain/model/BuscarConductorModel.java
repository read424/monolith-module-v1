package com.walrex.module_driver.domain.model;

import lombok.*;

/**
 * Modelo de dominio para representar los datos de un conductor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuscarConductorModel {

    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String apellidos;
    private String nombres;
    private String licencia;
}