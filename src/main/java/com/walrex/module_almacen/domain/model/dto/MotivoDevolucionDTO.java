package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * DTO para motivos de devolución
 * Value Object del dominio siguiendo el patrón DDD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDevolucionDTO {

    private Long id;

    private String descripcion;

    private Integer status;

    private OffsetDateTime createAt;

    private OffsetDateTime updateAt;
}
