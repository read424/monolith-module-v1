package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.RecetaPersistencePort;
import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.projection.RecetaProjection;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.RecetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RecetaPersistenceAdapter implements RecetaPersistencePort {

    private final RecetaRepository repository;

    @Override
    public Flux<Receta> findAll(String search, int page, int size) {
        long offset = (long) page * size;
        return repository.findAllPaged(search, offset, size)
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> count(String search) {
        return repository.countAll(search);
    }

    @Override
    public Mono<Receta> findById(Integer id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Receta> updateCurvaDiseno(Integer id, String curvaDiseno) {
        return repository.updateCurvaDiseno(id, curvaDiseno)
                .map(this::toDomain);
    }

    private Receta toDomain(RecetaProjection p) {
        return Receta.builder()
                .id(p.idReceta())
                .codReceta(p.codReceta())
                .razonSocial(p.razonSocial())
                .codColores(p.codColores())
                .noColores(p.noColores())
                .status(p.status())
                .compartir(parseCompartir(p.compartir()))
                .noGama(p.noGama())
                .noColor(p.noColor())
                .noTenido(p.noTenido())
                .curvaDiseno(p.curvaDiseno())
                .build();
    }

    /**
     * Convierte CHAR(1) de BD a Boolean.
     * Acepta: 'S'/'N', '1'/'0', 'T'/'F', 'Y'/'N', 'true'/'false'
     */
    private Boolean parseCompartir(String value) {
        if (value == null) return false;
        return switch (value.trim().toUpperCase()) {
            case "S", "1", "T", "Y", "TRUE" -> true;
            default -> false;
        };
    }
}
