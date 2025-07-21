package com.walrex.module_driver.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDomain {
    private Integer idDriver;
    private Integer idTipoDocumento;
    private String numDocumento;
    private String lastName;
    private String firstName;
    private String numLicencia;
}
