package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Slf4j
public class MovimientoIngresoAdapter extends BaseOrdenIngresoPersistenceAdapter {
    private final StandardKardexRegistrationStrategy kardexStrategy;
    private final InventoryRepository inventoryRepository;
    private final DetalleInventoryRespository detalleInventoryRepository;

    public MovimientoIngresoAdapter(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloAlmacenRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            StandardKardexRegistrationStrategy kardexStrategy,
            InventoryRepository inventoryRepository,
            DetalleInventoryRespository detalleInventoryRepository
            ) {

        super(ordenIngresoRepository, articuloRepository, detalleRepository,
                mapper, articuloIngresoLogisticaMapper);
        this.kardexStrategy = kardexStrategy;
        this.inventoryRepository=inventoryRepository;
        this.detalleInventoryRepository=detalleInventoryRepository;
    }

    @Override
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {

        return detalleInventoryRepository.getInventarioByDetailIngreso(savedDetalleEntity.getId().intValue())
                .flatMap(inventario -> {
                    detalle.setIdLoteInventario(inventario.getIdLote().intValue());
                    return inventoryRepository.getStockInStorage(savedDetalleEntity.getId_articulo(), ordenIngreso.getAlmacen().getIdAlmacen())
                            .flatMap(stockInfo -> {
                                detalle.getArticulo().setStock(stockInfo.getStock().subtract(BigDecimal.valueOf(savedDetalleEntity.getCantidad())).setScale(5, RoundingMode.HALF_UP));
                                return kardexStrategy.registrarKardex(savedDetalleEntity, detalle, ordenIngreso)
                                        .then(actualizarIdDetalle(detalle, savedDetalleEntity));
                            });
                });
    }
}
