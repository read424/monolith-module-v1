package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseInventarioAdapter {
    protected final ArticuloAlmacenRepository articuloRepository;

    /**
     * Procesa entrega y conversión (adaptado de OrdenSalidaTransformacionPersistenceAdapter)
     */
    protected Mono<DetalleEgresoDTO> procesarEntregaYConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        return buscarInfoConversion(detalle, ordenSalida)
                .flatMap(infoConversion -> aplicarConversion(detalle, infoConversion))
                .doOnSuccess(detalleActualizado ->
                        log.debug("✅ Conversión aplicada para artículo {}: stock={}",
                                detalleActualizado.getArticulo().getId(),
                                detalleActualizado.getArticulo().getStock()));
    }

    /**
     * Busca información de conversión por artículo
     */
    protected Mono<ArticuloInventory> buscarInfoConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso) {
        // ✅ Validaciones
        if (detalle == null) {
            return Mono.error(new IllegalArgumentException("El detalle no puede ser null"));
        }
        if (ordenEgreso == null) {
            return Mono.error(new IllegalArgumentException("La orden de egreso no puede ser null"));
        }
        if (ordenEgreso.getAlmacenOrigen() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("Almacén origen no puede ser null para la orden %d", ordenEgreso.getId()))
            );
        }
        if (detalle.getArticulo() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("Artículo no puede ser null para el detalle %d", detalle.getId())));
        }
        if (detalle.getArticulo().getId() == null) {
            return Mono.error(new IllegalArgumentException(
                    String.format("ID de artículo no puede ser null para el detalle %d", detalle.getId())));
        }

        Integer idAlmacen = ordenEgreso.getAlmacenOrigen().getIdAlmacen();
        Integer idArticulo = detalle.getArticulo().getId();

        log.debug("🔍 Buscando información de conversión para artículo {} en almacén {}", idArticulo, idAlmacen);

        return articuloRepository.getInfoConversionArticulo(idAlmacen, idArticulo)
                .doOnNext(info -> log.info("✅ Información de conversión encontrada: {}", info))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se encontró información de conversión para el artículo: " + idArticulo
                )));
    }

    /**
     * Aplica conversión de unidades
     */
    protected Mono<DetalleEgresoDTO> aplicarConversion(DetalleEgresoDTO detalle, ArticuloInventory infoConversion) {
        if (detalle.getIdUnidad() == null) {
            String errorMsg = String.format("ID de unidad no puede ser null para el detalle %d del artículo %d",
                    detalle.getId(), detalle.getArticulo().getId());
            log.error("❌ {}", errorMsg);
            return Mono.error(new IllegalArgumentException(errorMsg));
        }

        if (!detalle.getIdUnidad().equals(infoConversion.getIdUnidadConsumo())) {
            detalle.getArticulo().setIdUnidadSalida(infoConversion.getIdUnidadConsumo());
            detalle.getArticulo().setIs_multiplo(infoConversion.getIsMultiplo());
            detalle.getArticulo().setValor_conv(infoConversion.getValorConv());
            detalle.getArticulo().setStock(infoConversion.getStock());
        } else {
            detalle.getArticulo().setIdUnidadSalida(detalle.getIdUnidad());
        }

        return Mono.just(detalle);
    }
}