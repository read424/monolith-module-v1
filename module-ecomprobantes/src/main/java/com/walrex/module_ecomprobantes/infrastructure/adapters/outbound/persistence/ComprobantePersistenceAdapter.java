package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.ComprobantePersistencePort;
import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;
import com.walrex.module_ecomprobantes.domain.model.dto.DetalleComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.ComprobantePersistenceMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.DetalleComprobanteDTOMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.ComprobanteEntity;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComprobantePersistenceAdapter implements ComprobantePersistencePort {

        private final ComprobantesRepository comprobantesRepository;
        private final TipoSerieRepository tipoSerieRepository;
        private final DetalleComprobantesRepository detalleComprobantesRepository;
        private final ComprobantePersistenceMapper comprobantePersistenceMapper;
        private final DetalleComprobanteDTOMapper detalleMapper;

        @Override
        public Mono<ComprobanteDTO> crearComprobante(ComprobanteDTO comprobante) {
                log.info("💾 Creando comprobante Comprobante: {} para cliente: {} - Tipo: {}",
                                comprobante, comprobante.getIdCliente(), comprobante.getIdTipoComprobante());

                return tipoSerieRepository.findById(comprobante.getTipoSerie())
                                .switchIfEmpty(Mono.error(new RuntimeException(
                                                "No se encontró serie activa para el tipo de comprobante: "
                                                                + comprobante.getIdTipoComprobante())))
                                .flatMap(serie -> {
                                        log.debug("📋 Serie encontrada: {} - Número actual: {}", serie.getNuSerie(),
                                                        serie.getNuCompro());
                                        Integer siguienteNumero = serie.getNuCompro() + 1;
                                        comprobante.setNumeroComprobante(siguienteNumero);

                                        // Generar el código de comprobante con padding de 8 dígitos
                                        String correlativo8 = String.format("%08d", siguienteNumero);
                                        String codigoComprobante = serie.getNuSerie() + '-' + correlativo8;
                                        comprobante.setCodigoComprobante(codigoComprobante);
                                        log.info("🔢 Generando correlativo: Serie {} - Número: {} - CódigoComprobante: {}",
                                                        serie.getNuSerie(), correlativo8, codigoComprobante);

                                        serie.setNuCompro(siguienteNumero);
                                        return tipoSerieRepository.save(serie);
                                })
                                .flatMap(serieActualizada -> Mono
                                                .fromCallable(() -> comprobantePersistenceMapper.toEntity(comprobante))
                                                .flatMap(comprobantesRepository::save))
                                // 5. Setear el ID generado en el comprobante DTO
                                .doOnNext(comprobanteEntityGuardado -> {
                                        comprobante.setIdComprobante(comprobanteEntityGuardado.getIdComprobante());
                                        log.debug("🆔 ID generado para comprobante: {} - Cliente: {}",
                                                        comprobanteEntityGuardado.getIdComprobante(),
                                                        comprobante.getIdCliente());
                                })
                                // 6. TERCERO guardar los detalles usando el repository
                                .flatMap(comprobanteEntityGuardado -> {
                                        return guardarDetallesComprobante(
                                                        comprobanteEntityGuardado, comprobante.getDetalles())
                                                        .flatMap(idOrdenSalida -> {
                                                                comprobante.setIdOrdenSalida(idOrdenSalida);
                                                                return Mono.just(comprobante);
                                                        });
                                })
                                .doOnNext(resultado -> log.info(
                                                "✅ Comprobante creado con ID: {} - Cliente: {} - Número: {} - Detalles: {}",
                                                resultado.getIdComprobante(), resultado.getIdCliente(),
                                                resultado.getNumeroComprobante(), resultado.getDetalles().size()))
                                .doOnError(error -> log.error("❌ Error creando comprobante: {}", error.getMessage()));
        }

        /**
         * Guarda los detalles de un comprobante usando el repository
         * 
         * @param comprobanteEntity ComprobanteEntity guardado
         * @param detalles          Lista de detalles a guardar
         * @return Mono<Integer> devolviendo el id de la orden de salida
         */
        private Mono<Integer> guardarDetallesComprobante(ComprobanteEntity comprobanteEntity,
                        List<DetalleComprobanteDTO> detalles) {
                if (detalles == null || detalles.isEmpty()) {
                        log.error("❌ No se pueden guardar detalles vacíos o nulos - Comprobante: {}",
                                        comprobanteEntity.getIdComprobante());
                        return Mono.error(new IllegalArgumentException(
                                        "El comprobante debe tener al menos un detalle. Comprobante ID: " +
                                                        comprobanteEntity.getIdComprobante()));
                }

                log.info("💾 Guardando {} detalles para comprobante: {}", detalles.size(),
                                comprobanteEntity.getIdComprobante());

                return Mono.fromCallable(() -> {
                        return detalles.stream()
                                        .peek(detalle -> detalle.setIdComprobante(comprobanteEntity.getIdComprobante()))
                                        .map(detalleMapper::toEntity)
                                        .toList();
                })
                                .flatMapMany(entidades -> Flux.fromIterable(entidades))
                                .flatMap(detalleComprobantesRepository::save)
                                .doOnNext(entity -> log.debug(
                                                "✅ Detalle guardado - ID: {}, Producto: {}, Comprobante: {}",
                                                entity.getIdDetalleComprobante(), entity.getIdProducto(),
                                                entity.getIdComprobante()))
                                .collectList()
                                .map(savedEntities -> {
                                        return detalles.get(0).getIdOrdenSalida();
                                })
                                .doOnSuccess(v -> log.info(
                                                "✅ Todos los detalles guardados exitosamente - Comprobante: {}",
                                                comprobanteEntity.getIdComprobante()))
                                .doOnError(error -> log.error("❌ Error guardando detalles - Comprobante: {}, Error: {}",
                                                comprobanteEntity.getIdComprobante(), error.getMessage()));
        }
}