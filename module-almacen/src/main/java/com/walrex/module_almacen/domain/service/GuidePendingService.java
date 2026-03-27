package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.GuidePendingUseCase;
import com.walrex.module_almacen.application.ports.output.GuidePendingOutputPort;
import com.walrex.module_almacen.domain.model.GuidePendingRecord;
import com.walrex.module_almacen.domain.model.dto.GuidePendingDetail;
import com.walrex.module_almacen.domain.model.dto.GuidePendingResponse;
import com.walrex.module_almacen.domain.model.mapper.GuidePendingResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidePendingService implements GuidePendingUseCase {

    private final GuidePendingOutputPort outputPort;
    private final GuidePendingResponseMapper responseMapper;

    @Override
    public Flux<GuidePendingResponse> getPendingGuides(LocalDate date) {
        log.info("Consultando guías pendientes para la fecha: {}", date);

        return outputPort.findPendingGuides(date)
                .doOnNext(record -> log.info(
                        "GuidePendingService received domain ordenIngreso={}, detalle={}, peso_ref={}",
                        record.getId_ordeningreso(),
                        record.getId_detordeningreso(),
                        record.getPeso_ref()))
                .groupBy(GuidePendingRecord::getId_ordeningreso)
                .flatMap(groupedFlux -> groupedFlux.collectList().map(list -> buildResponse(groupedFlux.key(), list)));
    }

    private GuidePendingResponse buildResponse(Integer ordenIngresoId, List<GuidePendingRecord> list) {
        GuidePendingRecord first = list.get(0);
        log.info("GuidePendingService grouping ordenIngreso={} details={}", ordenIngresoId, list.size());

        List<GuidePendingDetail> details = list.stream()
                .map(responseMapper::toDetail)
                .peek(detail -> log.info(
                        "GuidePendingService mapped dto detail detalle={}, articulo={}, peso_ref={}",
                        detail.getId_detordeningreso(),
                        detail.getId_articulo(),
                        detail.getPeso_ref()))
                .toList();

        GuidePendingResponse response = responseMapper.toResponse(first, details);

        log.info("GuidePendingService built response ordenIngreso={} peso_ref values={}",
                response.getId_ordeningreso(),
                details.stream().map(GuidePendingDetail::getPeso_ref).toList());

        return response;
    }

    @Override
    public Flux<GuidePendingResponse> getPendingGuides(LocalDate date, Boolean isSupervisor) {
        log.info("GuidePendingService getPendingGuides with supervisor flag date={}, isSupervisor={}", date, isSupervisor);
        return getPendingGuides(date);
    }
}
