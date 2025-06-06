package com.walrex.module_almacen.infrastructure.config;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class IngresoPersistenceAdapterConfig {

    @Bean
    @Primary
    public OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloAlmacenRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            KardexRepository kardexRepository,
            DetailOrdenCompraAlmacenRepository detailOrdenCompraAlmacenRepository ) {

        return OrdenIngresoLogisticaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .kardexRepository(kardexRepository)
                .detailOrdenCompraAlmacenRepository(detailOrdenCompraAlmacenRepository)
                .build();
    }

    @Bean
    @Qualifier("telaCruda")
    public OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloAlmacenRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            DetalleRolloRepository detalleRolloRepository) {

        return OrdenIngresoTelaCrudaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .detalleRolloRepository(detalleRolloRepository)
                .build();
    }

    @Bean
    @Qualifier("transformacion")
    public OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloAlmacenRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            KardexRegistrationStrategy kardexStrategy,
            OrdenSalidaAdapterFactory salidaAdapterFactory) {

        return OrdenIngresoTransformacionPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .kardexStrategy(kardexStrategy)
                .salidaAdapterFactory(salidaAdapterFactory)
                .build();
    }

    @Bean
    public OrdenIngresoAdapterFactory ordenIngresoAdapterFactory(
            OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter,
            @Qualifier("telaCruda") OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter,
            @Qualifier("transformacion") OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter) {

        return new OrdenIngresoAdapterFactoryImpl(
                ordenIngresoLogisticaAdapter,
                ordenIngresoTelaCrudaAdapter,
                ordenIngresoTransformacionAdapter
        );
    }
}
