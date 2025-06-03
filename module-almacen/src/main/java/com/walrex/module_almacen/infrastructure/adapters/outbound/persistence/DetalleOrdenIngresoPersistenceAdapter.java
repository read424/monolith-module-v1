package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.GuardarDetalleOrdenIngresoPort;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.DetalleRollo;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetalleOrdenIngresoPersistenceAdapter implements GuardarDetalleOrdenIngresoPort {
    private final DetailsIngresoRepository detalleOrdenIngresoRepository;
    private final DetalleRolloRepository detalleRolloRepository;

    @Override
    public Flux<DetalleOrdenIngreso> guardarDetallesOrdenIngreso(List<DetalleOrdenIngreso> detalles, Integer idOrdenIngreso) {
        log.info("Guardando {} detalles para la orden de ingreso: {}", detalles.size(), idOrdenIngreso);
        return Flux.fromIterable(detalles)
                .flatMap(detalle -> {
                  //Creamos la entidad del detalle
                    DetailsIngresoEntity detalleEntity = DetailsIngresoEntity.builder()
                        .id_ordeningreso(idOrdenIngreso.longValue())
                        .id_articulo(detalle.getArticulo().getId())
                        .id_unidad(detalle.getIdUnidad())
                        .id_moneda(detalle.getIdMoneda())
                        .cantidad(detalle.getCantidad().doubleValue())
                        .costo_compra(detalle.getCosto().doubleValue())
                        .build();

                    return detalleOrdenIngresoRepository.save(detalleEntity)
                        .flatMap(savedDetalle ->{
                            // Si hay detalles de rollos, los guardamos también
                            if (detalle.getDetallesRollos() != null && !detalle.getDetallesRollos().isEmpty()) {
                                List<DetalleRolloEntity> rolloEntities = detalle.getDetallesRollos().stream()
                                    .map(rollo -> DetalleRolloEntity.builder()
                                        .id(null) // Asumimos que son rollos nuevos
                                        .codRollo(rollo.getCodRollo())
                                        .pesoRollo(rollo.getPesoRollo())
                                        .ordenIngreso(idOrdenIngreso)
                                        .idDetOrdenIngreso(savedDetalle.getId().intValue())
                                        .build())
                                    .collect(Collectors.toList());

                                return detalleRolloRepository.saveAll(rolloEntities)
                                    .collectList()
                                    .map(savedRollos -> {
                                        // Convertimos a dominio
                                        List<DetalleRollo> rollosDominio = savedRollos.stream()
                                                .map(rolloEntity -> DetalleRollo.builder()
                                                        .codRollo(rolloEntity.getCodRollo())
                                                        .pesoRollo(rolloEntity.getPesoRollo())
                                                        .ordenIngreso(rolloEntity.getOrdenIngreso())
                                                        .idDetOrdenIngreso(rolloEntity.getIdDetOrdenIngreso())
                                                        .build())
                                                .collect(Collectors.toList());

                                        // Construimos el objeto de dominio completo
                                        return DetalleOrdenIngreso.builder()
                                                .id(savedDetalle.getId().intValue())
                                                .articulo( Articulo.builder().id(savedDetalle.getId_articulo()).build())
                                                .idUnidad(savedDetalle.getId_unidad())
                                                .idMoneda(savedDetalle.getId_moneda())
                                                .cantidad(BigDecimal.valueOf(savedDetalle.getCantidad()))
                                                .costo(BigDecimal.valueOf(savedDetalle.getCosto_compra()))
                                                .montoTotal(BigDecimal.valueOf(savedDetalle.getCantidad()*savedDetalle.getCosto_compra()))
                                                .detallesRollos(rollosDominio)
                                                .build();
                                    });
                            }else{
                                // Si no hay rollos, simplemente convertimos el detalle
                                return Mono.just(DetalleOrdenIngreso.builder()
                                    .id(savedDetalle.getId().intValue())
                                    .articulo(Articulo.builder().id(savedDetalle.getId_articulo()).build())
                                    .idUnidad(savedDetalle.getId_unidad())
                                    .idMoneda(savedDetalle.getId_moneda())
                                    .cantidad(BigDecimal.valueOf(savedDetalle.getCantidad()))
                                    .costo(BigDecimal.valueOf(savedDetalle.getCosto_compra()))
                                    .montoTotal(BigDecimal.valueOf(savedDetalle.getCantidad()*savedDetalle.getCosto_compra()))
                                    .detallesRollos(List.of())
                                    .build());
                            }
                        });
                });
    }
}
