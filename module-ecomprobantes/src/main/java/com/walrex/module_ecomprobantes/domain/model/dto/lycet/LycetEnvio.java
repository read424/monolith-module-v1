package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

/**
 * DTO para el envío en la API de Lycet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetEnvio {

    private String codTraslado;
    private String modTraslado;
    private Double pesoTotal;
    private String undPesoTotal;
    private LocalDateTime fecTraslado;
    private LycetVehiculo vehiculo;
    private List<LycetChofer> choferes;
    private LycetDireccion llegada;
    private LycetDireccion partida;
}