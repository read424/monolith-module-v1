package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverDTO {
    private String typeDriver;
    private String typeDocument;
    private String descTypeDocument;
    private String abrevTypeDocument;
    private String numDocument;
    private String numLicencia;
    private String apellidos;
    private String nombres;
}
