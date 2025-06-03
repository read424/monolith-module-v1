package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.ObtenerArticuloPort;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticuloPersistenceAdapter  implements ObtenerArticuloPort {
    private final ArticuloAlmacenRepository articuloRepository;

    @Override
    public Mono<Articulo> obtenerArticuloPorId(Integer idAlmacen, Integer idArticulo) {
        return articuloRepository.getInfoConversionArticulo(idAlmacen, idArticulo)
                .map(result->Articulo.builder().build());
    }
}
