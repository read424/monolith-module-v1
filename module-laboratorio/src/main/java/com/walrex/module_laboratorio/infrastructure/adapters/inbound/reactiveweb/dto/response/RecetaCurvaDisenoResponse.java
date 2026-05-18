package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecetaCurvaDisenoResponse {
    private Integer id;
    private List<CurvaDisenoItemResponse> curvaDiseno;
}
