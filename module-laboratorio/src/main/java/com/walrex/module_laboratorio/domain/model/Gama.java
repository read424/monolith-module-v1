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
public class Gama {
    private Integer id;
    private String name;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Short order;
}
