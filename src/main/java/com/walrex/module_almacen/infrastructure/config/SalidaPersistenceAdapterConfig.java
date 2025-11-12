package com.walrex.module_almacen.infrastructure.config;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.mapper.ArticuloRequerimientoToDetalleMapper;
import com.walrex.module_almacen.domain.model.mapper.DetEgresoLoteEntityToItemKardexMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SalidaPersistenceAdapterConfig {

    @Bean
    @Primary
    public OrdenSalidaLogisticaPort ordenSalidaLogisticaPort(
            OrdenSalidaRepository ordenSalidaRepository,
            DetailSalidaRepository detalleSalidaRepository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper
            ) {

        return new OrdenSalidaLogisticaPersistenceAdapter(
                ordenSalidaRepository,
                detalleSalidaRepository,
                ordenSalidaEntityMapper,
                detailSalidaMapper
        );
    }

    @Bean
    @Qualifier("transformacionSalida")
    public OrdenSalidaLogisticaPort ordenSalidaTransformacionPort(
            OrdenSalidaRepository ordenSalidaRepository,
            DetailSalidaRepository detalleSalidaRepository,
            ArticuloAlmacenRepository articuloRepository,
            DetailSalidaLoteRepository detalleSalidaLoteRepository,
            DetalleInventoryRespository detalleInventoryRespository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper,
            KardexRegistrationStrategy kardexStrategy,
            DetEgresoLoteEntityToItemKardexMapper detEgresoLoteEntityToItemKardexMapper) {

        return new OrdenSalidaTransformacionPersistenceAdapter(
                ordenSalidaRepository,
                detalleSalidaRepository,
                articuloRepository,
                detalleSalidaLoteRepository,
                detalleInventoryRespository,
                ordenSalidaEntityMapper,
                detailSalidaMapper,
                kardexStrategy,
                detEgresoLoteEntityToItemKardexMapper
        );
    }

    @Bean
    @Qualifier("aprobacionSalida")
    public OrdenSalidaAprobacionPort ordenSalidaAprobarSalidaPort(
            ArticuloAlmacenRepository articuloRepository,
            OrdenSalidaRepository ordenSalidaRepository,
            DetailSalidaRepository detalleSalidaRepository,
            DetailSalidaLoteRepository detalleSalidaLoteRepository,
            DetalleInventoryRespository detalleInventoryRespository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper,
            KardexRepository kardexRepository,
            ArticuloRequerimientoToDetalleMapper articuloRequerimientoToDetalleMapper
    ){
        return new OrdenSalidaAprobacionPersistenceAdapter(
                articuloRepository,
                ordenSalidaRepository,
                detalleSalidaRepository,
                detalleSalidaLoteRepository,
                detalleInventoryRespository,
                ordenSalidaEntityMapper,
                detailSalidaMapper,
                kardexRepository,
                articuloRequerimientoToDetalleMapper
            );
    }

    @Bean
    public OrdenSalidaAdapterFactory ordenSalidaAdapterFactory(
            OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter,
            @Qualifier("transformacionSalida") OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter,
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort ordenSalidaAprobacionAdapter,
            @Qualifier("aprobacionMovimiento") OrdenSalidaAprobacionPort aprobacionMovimientoAdapter,
            @Qualifier("aprobacionInteligente") OrdenSalidaAprobacionPort aprobacionInteligenteAdapter) {

        return new OrdenSalidaAdapterFactoryImpl(
                ordenSalidaLogisticaAdapter,
                ordenSalidaTransformacionAdapter,
                ordenSalidaAprobacionAdapter,
                aprobacionMovimientoAdapter,
                aprobacionInteligenteAdapter
        );
    }

    @Bean
    @Qualifier("aprobacionInteligente")
    public OrdenSalidaAprobacionPort ordenSalidaAprobarInteligentePort(
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort aprobacionNormalAdapter,
            @Qualifier("aprobacionMovimiento") OrdenSalidaAprobacionPort aprobacionMovimientoAdapter,
            OrdenSalidaRepository ordenSalidaRepository
    ){
        return new OrdenSalidaAprobacionInteligenteAdapter(
                aprobacionNormalAdapter,
                aprobacionMovimientoAdapter,
                ordenSalidaRepository
        );
    }

    @Bean
    @Qualifier("aprobacionMovimiento")
    public OrdenSalidaAprobacionPort ordenSalidaAprobarMovimientoPort(
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort salidaAdapter,
            @Qualifier("ingresoMovimiento") OrdenIngresoLogisticaPort ingresoAdapter) {

        return new OrdenSalidaMovimientoAprobacionAdapter(salidaAdapter, ingresoAdapter);
    }
}