package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import java.util.List;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;

import lombok.*;

/**
 * Response DTO para la consulta de rollos disponibles para devolución
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarRollosDisponiblesResponse {
    private List<RolloDisponibleDevolucionDTO> rollosDisponibles;
    private Integer totalRollos;
    private boolean success;
    private String mensaje;
}