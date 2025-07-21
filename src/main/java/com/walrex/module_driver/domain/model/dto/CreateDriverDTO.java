package com.walrex.module_driver.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDriverDTO {
    private Integer idDriver;
    private Integer idTipoDocumento;
    private String numDocumento;
    private String apellidos;
    private String nombres;
    private String numLicencia;
    private Integer idUsuario;
}
