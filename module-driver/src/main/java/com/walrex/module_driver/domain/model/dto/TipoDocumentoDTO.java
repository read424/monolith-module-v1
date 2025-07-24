package com.walrex.module_driver.domain.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoDTO {
    private Integer idTipoDocumento;
    private String descTipoDocumento;
    private String abrevTipoDocumento;
}
