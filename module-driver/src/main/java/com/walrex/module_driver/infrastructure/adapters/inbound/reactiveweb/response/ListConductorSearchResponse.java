package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListConductorSearchResponse {
    private Integer total;
    private List<ConductorResponse> conductores;
}
