package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.GuidePendingUseCase;
import com.walrex.module_almacen.application.ports.output.GuidePendingOutputPort;
import com.walrex.module_almacen.domain.model.dto.GuidePendingDetail;
import com.walrex.module_almacen.domain.model.dto.GuidePendingResponse;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuidePendingProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidePendingService implements GuidePendingUseCase {

    private final GuidePendingOutputPort outputPort;

    @Override
    public Flux<GuidePendingResponse> getPendingGuides(LocalDate date) {
        log.info("Consultando guías pendientes para la fecha: {}", date);
        
        return outputPort.findPendingGuides(date)
                .groupBy(GuidePendingProjection::getId_ordeningreso)
                .flatMap(groupedFlux -> groupedFlux.collectList().map(list -> {
                    GuidePendingProjection first = list.get(0);
                    
                    return GuidePendingResponse.builder()
                            .id_ordeningreso(first.getId_ordeningreso())
                            .fec_registro(first.getFec_registro())
                            .nu_serie(first.getNu_serie())
                            .nu_comprobante(first.getNu_comprobante())
                            .razon_social(first.getRazon_social())
                            .details(list.stream().map(p -> GuidePendingDetail.builder()
                                    .id_detordeningreso(p.getId_detordeningreso())
                                    .lote(p.getLote())
                                    .id_articulo(p.getId_articulo())
                                    .cod_articulo(p.getCod_articulo())
                                    .desc_articulo(p.getDesc_articulo())
                                    .total_rollos(p.getTotal_rollos())
                                    .num_rollo(p.getNum_rollo())
                                    .rolls_saved(p.getRolls_saved())
                                    .build()).toList())
                            .build();
                }));
    }
}
