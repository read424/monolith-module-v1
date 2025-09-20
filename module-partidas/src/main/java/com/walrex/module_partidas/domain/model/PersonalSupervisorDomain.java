package com.walrex.module_partidas.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Modelo de dominio para personal supervisor
 * Representa la información del empleado que supervisa la operación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSupervisorDomain {

    /**
     * ID del personal
     */
    @NotNull(message = "El ID del personal es obligatorio")
    private Integer idPersonal;

    /**
     * Apellidos y nombres del empleado
     */
    @NotBlank(message = "El nombre del empleado es obligatorio")
    private String apenomEmpleado;
}
