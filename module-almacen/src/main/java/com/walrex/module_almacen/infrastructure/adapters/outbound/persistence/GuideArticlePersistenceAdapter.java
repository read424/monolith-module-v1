package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.UpdateGuideArticleOutputPort;
import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuideArticlePersistenceAdapter implements UpdateGuideArticleOutputPort {

    private final DetailsIngresoRepository detailsIngresoRepository;
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final DetalleRolloRepository detalleRolloRepository;

    @Override
    public Mono<Long> findIdOrdenIngresoByIdDetalleOrden(Integer idDetalleOrden) {
        return detailsIngresoRepository.findIdOrdenIngresoByIdDetalleOrden(idDetalleOrden);
    }

    @Override
    public Mono<Long> countExistingRolls(Integer idDetalleOrden) {
        return detailsIngresoRepository.countExistingRolls(idDetalleOrden)
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Boolean> existsProductionOrderByIdOrdenIngreso(Long idOrdenIngreso) {
        return ordenIngresoRepository.existsProductionOrderByIdOrdenIngreso(idOrdenIngreso)
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Boolean> existsAssignedPartidaByIdDetalleOrden(Integer idDetalleOrden) {
        return detalleRolloRepository.existsAssignedPartidaByIdDetalleOrden(idDetalleOrden)
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Void> updateGuideArticle(Integer idDetalleOrden, UpdateGuideArticleRequest request) {
        return detailsIngresoRepository.updateGuideArticle(
                        idDetalleOrden,
                        request.getId_articulo(),
                        request.getPeso_ref(),
                        request.getNu_rollos())
                .doOnNext(updatedRows -> log.info(
                        "Actualizando detalle de guía id_detordeningreso={}, filas_afectadas={}"
                        , idDetalleOrden,
                        updatedRows))
                .then();
    }
}
