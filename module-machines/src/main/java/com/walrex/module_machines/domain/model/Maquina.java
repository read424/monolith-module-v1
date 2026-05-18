package com.walrex.module_machines.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Maquina {
    private Integer idMaquina;
    private Integer idUbicacion;
    private String descMaq;
}
