package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.TipoSeriePersistencePort;
import com.walrex.module_ecomprobantes.domain.model.dto.TipoSerieDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.TipoSeriePersistenceMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository.TipoSerieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para TipoSerie.
 * 
 * Implementa el puerto TipoSeriePersistencePort utilizando el repository
 * reactivo.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TipoSeriePersistenceAdapter implements TipoSeriePersistencePort {

    private final TipoSerieRepository tipoSerieRepository;
    private final TipoSeriePersistenceMapper tipoSeriePersistenceMapper;

    @Override
    public Mono<TipoSerieDTO> guardarTipoSerie(TipoSerieDTO tipoSerieDTO) {
        log.info("💾 Guardando tipo de serie: {} - Comprobante: {}",
                tipoSerieDTO.getNuSerie(), tipoSerieDTO.getIdCompro());

        return Mono.fromCallable(() -> tipoSeriePersistenceMapper.toEntity(tipoSerieDTO))
                .flatMap(tipoSerieRepository::save)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(resultado -> log.info("✅ Tipo de serie guardado con ID: {} - Serie: {}",
                        resultado.getIdSerie(), resultado.getNuSerie()))
                .doOnError(error -> log.error("❌ Error guardando tipo de serie: {} - Error: {}",
                        tipoSerieDTO.getNuSerie(), error.getMessage()));
    }

    @Override
    public Mono<TipoSerieDTO> buscarTipoSeriePorId(Integer idSerie) {
        log.debug("🔍 Buscando tipo de serie por ID: {}", idSerie);

        return tipoSerieRepository.findById(idSerie)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(serie -> log.debug("✅ Tipo de serie encontrado: {} - Serie: {}",
                        serie.getIdSerie(), serie.getNuSerie()))
                .doOnError(error -> log.error("❌ Error buscando tipo de serie por ID: {} - Error: {}",
                        idSerie, error.getMessage()));
    }

    @Override
    public Mono<TipoSerieDTO> buscarTipoSeriePorNumero(String nuSerie) {
        log.debug("🔍 Buscando tipo de serie por número: {}", nuSerie);

        return tipoSerieRepository.findByNuSerie(nuSerie)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(serie -> log.debug("✅ Tipo de serie encontrado: {} - Serie: {}",
                        serie.getIdSerie(), serie.getNuSerie()))
                .doOnError(error -> log.error("❌ Error buscando tipo de serie por número: {} - Error: {}",
                        nuSerie, error.getMessage()));
    }

    @Override
    public Flux<TipoSerieDTO> buscarSeriesPorTipoComprobante(Integer idCompro) {
        log.debug("🔍 Buscando series por tipo de comprobante: {}", idCompro);

        return tipoSerieRepository.findByIdCompro(idCompro)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(serie -> log.debug("✅ Serie encontrada: {} - Tipo: {}",
                        serie.getNuSerie(), serie.getIdCompro()))
                .doOnError(error -> log.error("❌ Error buscando series por tipo de comprobante: {} - Error: {}",
                        idCompro, error.getMessage()));
    }

    @Override
    public Flux<TipoSerieDTO> buscarSeriesActivasPorTipoComprobante(Integer idCompro) {
        log.debug("🔍 Buscando series activas por tipo de comprobante: {}", idCompro);

        return tipoSerieRepository.findByIdComproAndIlEstadoTrue(idCompro)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(serie -> log.debug("✅ Serie activa encontrada: {} - Tipo: {}",
                        serie.getNuSerie(), serie.getIdCompro()))
                .doOnError(error -> log.error("❌ Error buscando series activas por tipo de comprobante: {} - Error: {}",
                        idCompro, error.getMessage()));
    }

    @Override
    public Flux<TipoSerieDTO> buscarSeriesPorTipoCPE(Integer isCpe) {
        log.debug("🔍 Buscando series por tipo CPE: {}", isCpe);

        return tipoSerieRepository.findByIsCpe(isCpe)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(serie -> log.debug("✅ Serie CPE encontrada: {} - CPE: {}",
                        serie.getNuSerie(), serie.getIsCpe()))
                .doOnError(error -> log.error("❌ Error buscando series por tipo CPE: {} - Error: {}",
                        isCpe, error.getMessage()));
    }

    @Override
    public Mono<TipoSerieDTO> actualizarTipoSerie(TipoSerieDTO tipoSerieDTO) {
        log.info("🔄 Actualizando tipo de serie: {} - ID: {}",
                tipoSerieDTO.getNuSerie(), tipoSerieDTO.getIdSerie());

        return Mono.fromCallable(() -> tipoSeriePersistenceMapper.toEntity(tipoSerieDTO))
                .flatMap(tipoSerieRepository::save)
                .map(tipoSeriePersistenceMapper::toDTO)
                .doOnNext(resultado -> log.info("✅ Tipo de serie actualizado: {} - Serie: {}",
                        resultado.getIdSerie(), resultado.getNuSerie()))
                .doOnError(error -> log.error("❌ Error actualizando tipo de serie: {} - Error: {}",
                        tipoSerieDTO.getNuSerie(), error.getMessage()));
    }

    @Override
    public Mono<Void> eliminarTipoSerie(Integer idSerie) {
        log.info("🗑️ Eliminando tipo de serie con ID: {}", idSerie);

        return tipoSerieRepository.deleteById(idSerie)
                .doOnSuccess(result -> log.info("✅ Tipo de serie eliminado con ID: {}", idSerie))
                .doOnError(error -> log.error("❌ Error eliminando tipo de serie con ID: {} - Error: {}",
                        idSerie, error.getMessage()));
    }
}