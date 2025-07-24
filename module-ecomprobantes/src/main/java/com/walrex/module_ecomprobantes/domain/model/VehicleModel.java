package com.walrex.module_ecomprobantes.domain.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel {
    private String numPlaca;
    private List<VehicleModel> vehicleSeconds;
}
