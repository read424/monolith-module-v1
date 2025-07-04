package com.walrex.module_almacen.domain.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.*;

/**
 * DTO del dominio para devolución de rollos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionRollosDTO {

    private Long idOrdenSalida;
    private Integer idMotivo;
    private Integer idAlmacenDestino;
    private Integer idCliente;
    private String observacion;
    private LocalDate fechaRegistro;
    private String codSalida;
    private Integer idUsuario;
    private List<RolloDevolucionDTO> rollos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolloDevolucionDTO {
        private Integer idDetOrdenIngresoPeso;
        private Integer idArticulo;
        private Integer idUnidad;
        private String codRollo;
        private BigDecimal pesoRollo;
        private Integer idDetPartida;
        private Integer idRolloIngreso;
        private String observaciones;
        private String sinCobro;
        private Integer statusRollPartida;
    }
}