package com.walrex.module_driver.domain.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConductorDataDTO {
    private TipoDocumentoDTO tipoDocumento;
    private String numeroDocumento;
    private String apellidos;
    private String nombres;
    private String numLicencia;
}
