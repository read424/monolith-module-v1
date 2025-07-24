package com.walrex.module_driver.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumento {
    private Integer idTipoDocumento;
    private String descTipoDocumento;
    private String abrevTipoDocumento;
}