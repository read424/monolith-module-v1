package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.ConsultarKardexUseCase;
import com.walrex.module_almacen.application.ports.output.KardexRepositoryPort;
import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.KardexReporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarKardexService implements ConsultarKardexUseCase {
    private final KardexRepositoryPort kardexRepository;

    @Override
    @Cacheable(value = "kardexMovimientos", key = "#criteriosBusquedaKardex.hashCode()")
    public Mono<KardexReporte> consultarKardex(CriteriosBusquedaKardex criteriosBusquedaKardex) {
        log.info("📋 Consultando kardex con cache para: {}", criteriosBusquedaKardex);
        return kardexRepository.consultarMovimientosKardex(criteriosBusquedaKardex)
                .map(articulos -> KardexReporte.builder()
                        .articulos(articulos)
                        .totalArticulos(articulos.size())
                        .fechaGeneracion(LocalDateTime.now())
                        .build()
                );
    }

    @CacheEvict(value = "kardexMovimientos", allEntries = true)
    public void invalidarTodoElCache() {
        log.info("🗑️ Cache de kardex invalidado completamente");
    }

    @CacheEvict(value = "kardexMovimientos", key = "#criterios.hashCode()")
    public void invalidarCachePorCriterios(CriteriosBusquedaKardex criterios) {
        log.info("🗑️ Cache invalidado para criterios: {}", criterios);
    }
}
