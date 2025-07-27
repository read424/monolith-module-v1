package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDTO {
    private String numDocumento;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String telefono;
    private String tlf_fax;
    private String tlf_movil;
    private String siteWeb;
    private String email;
}
