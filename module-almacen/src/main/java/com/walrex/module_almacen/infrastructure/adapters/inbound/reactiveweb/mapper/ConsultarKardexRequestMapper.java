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

        /*
        kardexRequest.setIdArticulo(
                Integer.valueOf(request.queryParam("id_articulo")
                        .orElseThrow(() -> new IllegalArgumentException("id_articulo es requerido")))
        );
         */

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
