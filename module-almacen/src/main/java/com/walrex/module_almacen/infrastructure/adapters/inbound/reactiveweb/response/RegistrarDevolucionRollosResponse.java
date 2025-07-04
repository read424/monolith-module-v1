package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

/**
 * Response para el registro de devolución de rollos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarDevolucionRollosResponse {

    private Long idOrdenSalida;
    private String codSalida;
    private LocalDate fechaRegistro;
    private String observacion;
    private Integer totalRollos;
    private List<RolloDevueltoResponse> rollosDevueltos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolloDevueltoResponse {
        private String codRollo;
        private String pesoRollo;
        private String articulo;
        private String status;
        private String observaciones;
    }
}