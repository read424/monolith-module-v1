package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.input.ProcesarTransformacionUseCase;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import com.walrex.module_almacen.domain.model.mapper.OrdenIngresoTransformacionMapper;
import com.walrex.module_almacen.domain.model.mapper.OrdenSalidaTransformacionMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarTransformacionService implements ProcesarTransformacionUseCase {
    private final OrdenIngresoAdapterFactory ingresoAdapterFactory;
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;
    private final OrdenIngresoTransformacionMapper ordenIngresoTransformacionMapper;
    private final OrdenSalidaTransformacionMapper ordenSalidaTransformacionMapper;

    @Override
    @Transactional
    public Mono<TransformacionResponseDTO> procesarTransformacion(OrdenIngresoTransformacionDTO request) {
        log.info("Iniciando proceso de transformación para artículo: {}",
                request.getArticulo().getIdArticulo());
        return procesarIngreso(request)
                .flatMap(ingresoCreado -> procesarSalidas(request, ingresoCreado))
                .map(response -> TransformacionResponseDTO.builder()
                        .id_transformacion(1L)
                        .build()
                ).doOnSuccess(response->
                    log.info("✅ Transformación completada exitosamente: {}", response.getId_transformacion())
                ).doOnError(error ->
                    log.error("❌ Error en proceso de transformación: {}", error.getMessage(), error)
                );
    }

    private Mono<OrdenIngreso> procesarIngreso(OrdenIngresoTransformacionDTO request) {
        log.debug("Procesando ingreso de transformación");
        // Mapear OrdenIngresoTransformacionDTO → OrdenIngreso
        OrdenIngreso ordenIngreso = ordenIngresoTransformacionMapper.toOrdenIngreso(request);

        return ingresoAdapterFactory.getAdapter(TipoOrdenIngreso.TRANSFORMACION)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngreso));
    }

    private Mono<TransformacionProcesoResponseDTO> procesarSalidas(OrdenIngresoTransformacionDTO request, OrdenIngreso ingresoCreado) {
        log.debug("Procesando salidas de transformación para ingreso: {}", ingresoCreado.getId());
        // ✅ Mapear la request de transformación a orden de salida
        OrdenEgresoDTO ordenSalida = ordenSalidaTransformacionMapper.toOrdenEgreso(request);
        return salidaAdapterFactory.getAdapter(TipoOrdenSalida.TRANSFORMACION)
                .flatMap(adapter -> adapter.guardarOrdenSalida(ordenSalida))
                .map(salidaCreada -> TransformacionProcesoResponseDTO.builder()
                        .id(ingresoCreado.getId().longValue())
                        .codIngreso(ingresoCreado.getCod_ingreso())
                        .idOrdensalida(salidaCreada.getId())
                        .codEgreso(salidaCreada.getCodEgreso())
                        .articuloProducido(request.getArticulo())
                        .cantidadProducida(request.getCantidad())
                        .insumosConsumidos(salidaCreada.getDetalles())
                        .build()
                )
                .doOnSuccess(resultado ->
                        log.info("✅ Salidas procesadas - Egreso: {}, Insumos consumidos: {}",
                                resultado.getCodEgreso(), resultado.getInsumosConsumidos().size()));
    }
}
