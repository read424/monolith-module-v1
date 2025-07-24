package com.walrex.module_ecomprobantes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPersonModel {
    private Integer idTipoDocumento;
    private String numDocumento;
    private String apellidos;
    private String nombres;
    private String numLicencia;
}
