package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO para información del personal supervisor
 * Contiene datos del empleado que supervisa la operación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSupervisor {

    /**
     * ID del personal
     */
    @JsonProperty("id_personal")
    private Integer idPersonal;

    /**
     * Apellidos y nombres del empleado
     */
    @JsonProperty("apenom_empleado")
    private String apenomEmpleado;
}
