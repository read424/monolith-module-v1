package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.AprobarSalidaInsumosUseCase;
import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AprobarSalidaInsumosService implements AprobarSalidaInsumosUseCase {
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;

    @Override
    public Mono<OrdenEgresoDTO> aprobarSalidaInsumos(AprobarSalidaRequerimiento dto) {
        log.info("Iniciando aprobación de salida de insumos para orden: {}", dto.getIdOrdenSalida());

        // ✅ Filtrar productos seleccionados
        List<ArticuloRequerimiento> productosSeleccionados = filtrarProductosSeleccionados(dto.getDetalles());

        log.info("Productos a aprobar: {}", productosSeleccionados.size());

        return salidaAdapterFactory.getAprobacionAdapter(TipoOrdenSalida.APPROVE_DELIVERY)
                .flatMap(adapter -> adapter.procesarAprobacionCompleta(dto, productosSeleccionados))
                .doOnSuccess(response ->
                        log.info("✅ Aprobación completada para orden: {} - Código: {}",
                                dto.getIdOrdenSalida(), response.getCodEgreso()))
                .doOnError(error ->
                        log.error("❌ Error en aprobación de salida para orden: {} - Error: {}",
                                dto.getIdOrdenSalida(), error.getMessage(), error));
    }

    private List<ArticuloRequerimiento> filtrarProductosSeleccionados(List<ArticuloRequerimiento> productos) {
        if (productos == null) {
            throw new IllegalArgumentException("La lista de productos no puede ser null");
        }

        List<ArticuloRequerimiento> productosSeleccionados = productos.stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getSelected()))
                .collect(Collectors.toList());

        if (productosSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron productos seleccionados en la lista");
        }

        return productosSeleccionados;
    }
}
