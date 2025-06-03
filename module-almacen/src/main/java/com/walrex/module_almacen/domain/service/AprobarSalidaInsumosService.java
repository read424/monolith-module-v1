package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.AprobarSalidaInsumosUseCase;
import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import com.walrex.module_almacen.domain.model.mapper.ArticuloRequerimientoToDetalleMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenSalidaAprobacionPersistenceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AprobarSalidaInsumosService implements AprobarSalidaInsumosUseCase {
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;
    private final ArticuloRequerimientoToDetalleMapper articuloRequerimientoMapper;

    @Override
    @Transactional
    public Mono<ResponseAprobacionRequerimientoDTO> aprobarSalidaInsumos(AprobarSalidaRequerimiento dto) {
        log.info("Iniciando aprobación de salida de insumos para orden: {}", dto.getIdOrdenSalida());

        // ✅ Filtrar productos seleccionados
        List<ArticuloRequerimiento> productosSeleccionados = filtrarProductosSeleccionados(dto.getDetalles());

        log.info("Productos a aprobar: {}", productosSeleccionados.size());

        return salidaAdapterFactory.getAdapter(TipoOrdenSalida.APPROVE_DELIVERY)
                .flatMap(adapter -> procesarAprobacion(dto, productosSeleccionados, adapter))
                .doOnSuccess(response ->
                        log.info("✅ Aprobación completada para orden: {} - Productos aprobados: {}",
                                dto.getIdOrdenSalida(), response.getProductosAprobados()))
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

    private Mono<ResponseAprobacionRequerimientoDTO> procesarAprobacion(
            AprobarSalidaRequerimiento request,
            List<ArticuloRequerimiento> productosSeleccionados,
            OrdenSalidaLogisticaPort adapter) {

        Integer idOrden = request.getIdOrdenSalida();

        return adapter.consultarYValidarOrdenParaAprobacion(idOrden)
                .flatMap(ordenEgreso->{
                    log.info("✅ Orden {} validada, procesando {} productos",
                            idOrden, productosSeleccionados.size());
                    return Flux.fromIterable(productosSeleccionados)
                            .flatMap(articulo -> procesarDetalleAprobacion(articulo, ordenEgreso, adapter))
                            .collectList()
                            .flatMap(detallesProcesados ->
                                    // ✅ Actualizar estado de entrega de la orden completa
                                    adapter.actualizarEstadoEntrega(idOrden, true)
                                            .map(ordenActualizada -> construirRespuestaExitosa(
                                                    request,
                                                    productosSeleccionados,
                                                    detallesProcesados,
                                                    ordenActualizada))
                            );
                })
                .onErrorResume(error -> {
                    log.error("Error al procesar aprobación para orden: {}", idOrden, error);
                    return Mono.just(construirRespuestaError(request, error.getMessage()));
                });
    }

    // ✅ Método auxiliar para procesar cada detalle
    private Mono<DetalleEgresoDTO> procesarDetalleAprobacion(
            ArticuloRequerimiento articulo,
            OrdenEgresoDTO ordenEgreso,
            OrdenSalidaLogisticaPort adapter) {

        // ✅ Mapear ArticuloRequerimiento → DetalleEgresoDTO
        DetalleEgresoDTO detalle = articuloRequerimientoMapper.toDetalleEgreso(articulo);

        // ✅ Usar el adapter específico de aprobación
        if (adapter instanceof OrdenSalidaAprobacionPersistenceAdapter) {
            OrdenSalidaAprobacionPersistenceAdapter aprobacionAdapter =
                    (OrdenSalidaAprobacionPersistenceAdapter) adapter;
            return aprobacionAdapter.procesarAprobacionDetalle(detalle, ordenEgreso);
        } else {
            return Mono.error(new IllegalStateException("Adapter incorrecto para proceso de aprobación"));
        }
    }

    private ResponseAprobacionRequerimientoDTO construirRespuestaExitosa(
            AprobarSalidaRequerimiento request,
            List<ArticuloRequerimiento> productosSeleccionados,
            List<DetalleEgresoDTO> detallesProcesados,
            OrdenEgresoDTO ordenActualizada) {

        return ResponseAprobacionRequerimientoDTO.builder()
                .success(true)
                .message("Salida de insumos aprobada exitosamente")
                .idOrdenSalida(request.getIdOrdenSalida())
                .codigoSalida(ordenActualizada.getCodEgreso())
                .productosAprobados(productosSeleccionados.size())
                .detalleAprobacion(mapearProductosAprobados(productosSeleccionados))
                .fechaAprobacion(OffsetDateTime.now())
                .build();
    }

    private ResponseAprobacionRequerimientoDTO construirRespuestaError(
            AprobarSalidaRequerimiento request,
            String mensajeError) {

        return ResponseAprobacionRequerimientoDTO.builder()
                .success(false)
                .message("Error al aprobar salida: " + mensajeError)
                .idOrdenSalida(request.getIdOrdenSalida())
                .productosAprobados(0)
                .fechaAprobacion(OffsetDateTime.now())
                .build();
    }

    private List<ProductoAprobadoDTO> mapearProductosAprobados(List<ArticuloRequerimiento> productos) {
        return productos.stream()
                .map(articuloRequerimientoMapper::toProductoAprobado)
                .collect(Collectors.toList());
    }
}
