package com.walrex.module_laboratorio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurvaDisenoItem {
    private Integer id;
    private Integer idCurvaDiseno;
    private String curva;
}
