package com.walrex.module_partidas.domain.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessPartidaTacho {

    /**
     * ID de la partida
    */
    @NotNull(message = "El id partida es obligatorio")
    private Integer idPartida;

    /**
     * ID del almacén
    */
    @NotNull(message = "El id almacen es obligatorio")
    private Integer idAlmacen;

    /**
     * ID del cliente
    */
    @NotNull(message = "El id cliente es obligatorio")
    private Integer idCliente;
    
    /**
     * ID del artículo
    */
    @NotNull(message = "El id articulo es obligatorio")
    private Integer idArticulo;

    /**
     * Lote
    */
    @NotNull(message = "El lote es obligatorio")
    private String lote;

    /**
     * ID de la unidad
    */
    @NotNull(message = "El id unidad es obligatorio")
    private Integer idUnidad;
    
    /**
     * ID del supervisor
    */
    @NotNull(message = "El id supervisor es obligatorio")
    private Integer idSupervisor;

    /**
     * Observación
    */
    private String observacion;

    /**
     * Lista de rollos
    */
    @Valid
    private List<ItemRolloProcess> rollos;
}
