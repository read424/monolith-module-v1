package com.walrex.module_ecomprobantes.domain.model.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehiclesDTO {
    private VehicleDTO principal;
    private List<VehicleDTO> secundarios;
}
