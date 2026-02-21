package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.GuideAdditionOutputPort;
import com.walrex.module_almacen.domain.model.dto.AddGuideDetail;
import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
import com.walrex.module_almacen.domain.model.dto.AddGuideResponse;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuideAdditionPersistenceAdapter implements GuideAdditionOutputPort {

    private final OrdenIngresoRepository ordenIngresoRepository;
    private final DetailsIngresoRepository detailsIngresoRepository;

    @Override
    @Transactional
    public Mono<AddGuideResponse> saveGuide(AddGuideRequest request) {
        log.info("Guardando nueva guía: {} - {}", request.getNu_serie(), request.getNu_comprobante());

        // Generar un código de ingreso temporal o final
        String codIngreso = "ING-" + System.currentTimeMillis();

        OrdenIngresoEntity mainEntity = OrdenIngresoEntity.builder()
                .id_cliente(request.getId_cliente())
                .id_comprobante(request.getId_comprobante())
                .nu_comprobante(request.getNu_comprobante())
                .nu_serie(request.getNu_serie())
                .fec_ingreso(request.getFec_registro() != null ? LocalDate.parse(request.getFec_registro()) : LocalDate.now())
                .cod_ingreso(codIngreso)
                .id_almacen(2) // Según el contexto previo del usuario
                .status(1)
                .id_motivo(1) // Por defecto: Ingreso
                .build();

        return ordenIngresoRepository.save(mainEntity)
                .flatMap(savedMain -> {
                    log.info("Guía principal guardada con ID: {}", savedMain.getId());
                    
                    return Flux.fromIterable(request.getDetalles())
                            .flatMap(detail -> {
                                DetailsIngresoEntity detailEntity = DetailsIngresoEntity.builder()
                                        .id_ordeningreso(savedMain.getId())
                                        .id_articulo(detail.getId_articulo())
                                        .cantidad(detail.getCnt_rollos().doubleValue())
                                        .peso_ref(detail.getTotal_kg())
                                        .peso_ingreso(0.0) // Se llenará con el pesaje
                                        .id_unidad(1) // Unidad por defecto
                                        .status(1L)
                                        .build();
                                return detailsIngresoRepository.save(detailEntity);
                            })
                            .collectList()
                            .map(savedDetails -> AddGuideResponse.builder()
                                    .id_ordeningreso(savedMain.getId().intValue())
                                    .cod_ingreso(savedMain.getCod_ingreso())
                                    .nu_serie(savedMain.getNu_serie())
                                    .nu_comprobante(savedMain.getNu_comprobante())
                                    .id_comprobante(savedMain.getId_comprobante())
                                    .id_cliente(savedMain.getId_cliente())
                                    .detalles(request.getDetalles())
                                    .build());
                });
    }
}
