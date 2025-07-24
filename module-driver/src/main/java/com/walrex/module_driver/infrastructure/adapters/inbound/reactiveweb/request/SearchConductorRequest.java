package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El número de documento es obligatorio")
    private String numDoc;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Integer idTipDoc;
}