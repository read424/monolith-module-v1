package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request;

import lombok.*;

/**
 * DTO para la búsqueda de conductores por número de documento y tipo de
 * documento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchConductorRequest {

    private String numDoc;

    private Integer idTipDoc;

    private String name;    
}