package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.ConsultarKardexUseCase;
import com.walrex.module_almacen.application.ports.output.KardexRepositoryPort;
import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.KardexReporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;



@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarKardexService implements ConsultarKardexUseCase {
    private final KardexRepositoryPort kardexRepository;

    @Override
    public Mono<KardexReporte> consultarKardex(CriteriosBusquedaKardex criteriosBusquedaKardex) {
        return kardexRepository.consultarMovimientosKardex(criteriosBusquedaKardex)
                .map(articulos -> KardexReporte.builder()
                        .articulos(articulos)
                        .totalArticulos(articulos.size())
                        .fechaGeneracion(LocalDateTime.now())
                        .build()
                );
    }
}
