package com.walrex.module_ecomprobantes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarrierModel {
    private String tipoDocumento;
    private String numDocumento;
    private String razonSocial;
    private String nroMTC;
}
