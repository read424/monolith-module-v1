package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("laboratorio.tbgamas")
public class GamaEntity {
    @Id
    @Column("id_gama")
    private Integer idGama;
    
    @Column("no_gama")
    private String noGama;
    
    private Integer status;
    
    @Column("created_at")
    private OffsetDateTime createdAt;
    
    @Column("updated_at")
    private OffsetDateTime updatedAt;
    
    @Column("i_orden")
    private Short order;
}
