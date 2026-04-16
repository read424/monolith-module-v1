package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("laboratorio.etapatintura")
public class EtapaTinturaEntity {
    @Id
    private Integer id_tintura;
    private Integer id_proceso;
    private String desc_tintura;
    private String observacion;
    private LocalDate fec_registro;
    private Integer status;
    private Integer id_usuario;
}
