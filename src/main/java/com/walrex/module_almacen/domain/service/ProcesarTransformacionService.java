package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.input.ProcesarTransformacionUseCase;
import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Motivo;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.domain.model.enums.*;
import com.walrex.module_almacen.domain.model.mapper.OrdenIngresoTransformacionMapper;
import com.walrex.module_almacen.domain.model.mapper.OrdenSalidaTransformacionMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    public Mono<TransformacionResponseDTO> procesarTransformacion(OrdenIngresoTransformacionDTO request) {
        log.info("Iniciando proceso de transformación para artículo: {}",
                request.getArticulo().getIdArticulo());
        return procesarIngreso(request)
                .flatMap(ingresoCreado -> procesarSalidas(request, ingresoCreado)
                        .map(salidaCreada->TransformacionResponseDTO.builder()
                                .codigoIngreso(ingresoCreado.getCod_ingreso())
                                .codigoSalida(salidaCreada.getCodEgreso())
                                .build()
                        )
                )
                .doOnSuccess(response->
                    log.info("✅ Transformación completada exitosamente: {}", response.getCodigoSalida())
                )
                .doOnError(error ->
                    log.error("❌ Error en proceso de transformación: {}", error.getMessage(), error)
                );
    }

    private Mono<OrdenIngreso> procesarIngreso(OrdenIngresoTransformacionDTO request) {
        log.debug("Procesando ingreso de transformación");
        // Mapear OrdenIngresoTransformacionDTO → OrdenIngreso
        log.info("informacion a mapear to OrdenIngreso {}: ", request);
        OrdenIngreso ordenIngreso = ordenIngresoTransformacionMapper.toOrdenIngreso(request);
        ordenIngreso.setMotivo(Motivo.builder()
                .idMotivo(TypeMotivoIngreso.TRANSFORMACION.getId())
                .descMotivo(TypeMotivoIngreso.TRANSFORMACION.getDescMotivo())
                .build()
        );
        Almacen almacen = ordenIngreso.getAlmacen();
        if(almacen.getIdAlmacen()==null) {
            ordenIngreso.setAlmacen(
                    Almacen.builder()
                            .idAlmacen(TypeAlmacen.INSUMOS.getId())
                            .build()
            );
        }
        log.info("informacion OrdenIngreso {}: ", ordenIngreso);
        return ingresoAdapterFactory.getAdapter(TipoOrdenIngreso.TRANSFORMACION)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngreso));
    }

    private Mono<TransformacionProcesoResponseDTO> procesarSalidas(OrdenIngresoTransformacionDTO request, OrdenIngreso ingresoCreado) {
        log.debug("Procesando salidas de transformación para ingreso: {}", ingresoCreado.getId());
        log.info("request: {}", request);
        // ✅ Mapear la request de transformación a orden de salida
        OrdenEgresoDTO ordenSalida = ordenSalidaTransformacionMapper.toOrdenEgreso(request);
        ordenSalida.setMotivo(
                Motivo.builder()
                        .idMotivo(TypeMotivoEgreso.TRANSFORMACION.getId())
                        .descMotivo(TypeMotivoEgreso.TRANSFORMACION.getDescMotivo())
                        .build()
        );
        log.info("mapper OrdenIngresoTransformacionDTO to OrdenEgresoDTO {} :", ordenSalida);
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
