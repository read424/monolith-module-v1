package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarrierDTO {
    private String typeDocument;
    private String abrevDoc;
    private String descTypeDocument;
    private String numDocument;
    private String razonSocial;
    private String nroMTC;
}
