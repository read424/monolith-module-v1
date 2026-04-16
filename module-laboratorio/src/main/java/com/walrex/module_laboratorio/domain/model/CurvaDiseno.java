package com.walrex.module_laboratorio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurvaDiseno {
    private Integer id;
    private String descripcion;
    private String curvaDiseno;
    private String version;
    private Integer idLaboratorista;
    private String laboratorista;
    private Integer idSupervisor;
    private String supervisor;
    private Integer status;
    private Boolean locked;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
