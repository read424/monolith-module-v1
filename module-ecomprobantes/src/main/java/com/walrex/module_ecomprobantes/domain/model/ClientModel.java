package com.walrex.module_ecomprobantes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientModel {
    private String tipDocument;
    private String numDocument;
    private String razonSocial;
}
