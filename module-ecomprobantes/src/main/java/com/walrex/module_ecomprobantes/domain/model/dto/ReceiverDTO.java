package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiverDTO {
    private String codTipodoc;
    private String tipoDocumento;
    private String numDocumento;
    private String razonSocial;
    private DireccionDTO direccion;
}
