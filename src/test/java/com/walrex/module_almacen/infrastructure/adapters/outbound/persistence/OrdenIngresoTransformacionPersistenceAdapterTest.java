package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.enums.TypeAlmacen;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrdenIngresoTransformacionPersistenceAdapterTest {

    @Mock
    private OrdenIngresoRepository ordenIngresoRepository;

    @Mock
    private ArticuloAlmacenRepository articuloRepository;

    @Mock
    private DetailsIngresoRepository detalleRepository;

    @Mock
    private OrdenIngresoEntityMapper mapper;

    @Mock
    private ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    @Mock
    private KardexRegistrationStrategy kardexStrategy;

    @Mock
    private OrdenSalidaAdapterFactory salidaAdapterFactory;

    private OrdenIngresoTransformacionPersistenceAdapter adapter;

    private OrdenIngreso ordenIngreso;
    private OrdenIngresoEntity ordenIngresoEntity;
    private DetalleOrdenIngreso detalle;
    private DetailsIngresoEntity detalleEntity;

    @BeforeEach
    void setUp() {
        adapter = OrdenIngresoTransformacionPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .detalleRepository(detalleRepository)
                .articuloRepository(articuloRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .kardexStrategy(kardexStrategy)
                .salidaAdapterFactory(salidaAdapterFactory)
                .build();

        // Preparar datos de prueba
        setupTestData();
    }

    @Test
    void deberiaGuardarOrdenTransformacionExitosamente() {
        // Given
        ArticuloInventory articuloConversion = ArticuloInventory.builder()
                .idUnidad(1)
                .idUnidadConsumo(6)
                .isMultiplo("1")
                .valorConv(3)
                .stock(BigDecimal.valueOf(100.00))
                .build();

        when(mapper.toEntity(any(OrdenIngreso.class))).thenReturn(ordenIngresoEntity);
        when(ordenIngresoRepository.save(any(OrdenIngresoEntity.class))).thenReturn(Mono.just(ordenIngresoEntity));
        when(mapper.toDomain(any(OrdenIngresoEntity.class))).thenReturn(ordenIngreso);
        when(articuloRepository.getInfoConversionArticulo(anyInt(), anyInt())).thenReturn(Mono.just(articuloConversion));
        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.just(detalleEntity));
        when(kardexStrategy.registrarKardex(any(), any(), any())).thenReturn(Mono.empty());

        // When
        Mono<OrdenIngreso> resultado = adapter.guardarOrdenIngresoLogistica(ordenIngreso);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngreso)
                .verifyComplete();

        verify(ordenIngresoRepository).save(any(OrdenIngresoEntity.class));
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));
        verify(kardexStrategy).registrarKardex(any(), any(), any());
    }

    private void setupTestData() {
        // Preparar datos de prueba
        ordenIngreso = OrdenIngreso.builder()
                .fechaIngreso(LocalDate.of(2025, 5, 3))
                .almacen(Almacen.builder()
                        .idAlmacen(TypeAlmacen.INSUMOS.getId()).build()
                )
                .detalles(List.of(
                    DetalleOrdenIngreso.builder()
                        .articulo(Articulo.builder()
                            .id(242)
                            .build()
                        )
                        .cantidad(BigDecimal.valueOf(120))
                        .costo(BigDecimal.valueOf(1.75))
                        .idUnidad(1)
                        .build()
                ))
                .build();

        ordenIngresoEntity = new OrdenIngresoEntity();
        ordenIngresoEntity.setId_ordeningreso(1L);
        ordenIngresoEntity.setCod_ingreso("ALTI-I00004");

        detalleEntity = new DetailsIngresoEntity();
        detalleEntity.setId(1L);
        detalleEntity.setId_ordeningreso(1L);
    }
}
