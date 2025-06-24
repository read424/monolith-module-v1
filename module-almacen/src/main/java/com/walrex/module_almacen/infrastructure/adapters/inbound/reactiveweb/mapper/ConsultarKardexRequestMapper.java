package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarKardexRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDate;

@Component
public class ConsultarKardexRequestMapper {

    public ConsultarKardexRequest extractFromQuery(ServerRequest request) {
        ConsultarKardexRequest kardexRequest = new ConsultarKardexRequest();

        kardexRequest.setIdArticulo(
                request.queryParam("id_articulo")
                        .map(value->{
                            try {
                                return Integer.valueOf(value);
                            } catch (NumberFormatException e) {
                                return null; // Si no puede parsear, devuelve null
                            }
                        })
                        .orElse(null)
        );

        kardexRequest.setIdAlmacen(
                Integer.valueOf(request.queryParam("id_almacen")
                        .orElseThrow(() -> new IllegalArgumentException("id_almacen es requerido")))
        );

        kardexRequest.setFechaInicio(
                LocalDate.parse(request.queryParam("fecha_inicio")
                        .orElseThrow(() -> new IllegalArgumentException("fecha_inicio es requerida")))
        );

        kardexRequest.setFechaFin(
                LocalDate.parse(request.queryParam("fecha_fin")
                        .orElseThrow(() -> new IllegalArgumentException("fecha_fin es requerida")))
        );

        return kardexRequest;
    }

    public CriteriosBusquedaKardex toCriterios(ConsultarKardexRequest request) {
        CriteriosBusquedaKardex criterios = new CriteriosBusquedaKardex();
        criterios.setIdArticulo(request.getIdArticulo());
        criterios.setIdAlmacen(request.getIdAlmacen());
        criterios.setFechaInicio(request.getFechaInicio());
        criterios.setFechaFin(request.getFechaFin());
        return criterios;
    }
}
