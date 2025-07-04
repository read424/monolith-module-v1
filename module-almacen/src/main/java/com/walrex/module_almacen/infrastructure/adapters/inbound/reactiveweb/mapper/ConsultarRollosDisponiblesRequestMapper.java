package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarRollosDisponiblesRequest;

/**
 * Mapper para convertir parámetros de query a DTO de request
 * Sigue el patrón Mapper Pattern para separar responsabilidades
 */
@Component
public class ConsultarRollosDisponiblesRequestMapper {

    /**
     * Extrae parámetros de query de la ServerRequest y los convierte a DTO
     */
    public ConsultarRollosDisponiblesRequest extractFromQuery(ServerRequest request) {
        var queryParams = request.queryParams();

        return ConsultarRollosDisponiblesRequest.builder()
                .idCliente(extraerParametroInteger(queryParams.getFirst("id_cliente")))
                .idArticulo(extraerParametroInteger(queryParams.getFirst("id_articulo")))
                .build();
    }

    private Integer extraerParametroInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido: " + value);
        }
    }
}
