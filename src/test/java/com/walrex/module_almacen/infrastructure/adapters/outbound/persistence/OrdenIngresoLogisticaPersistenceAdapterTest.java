package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import io.r2dbc.spi.R2dbcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenIngresoLogisticaPersistenceAdapterTest {
    @Mock
    private OrdenIngresoRepository ordenIngresoRepository;

    @Mock
    private ArticuloAlmacenRepository articuloRepository;

    @Mock
    private DetailsIngresoRepository detalleRepository;

    @Mock
    private KardexRepository kardexRepository;

    @Mock
    private OrdenIngresoEntityMapper mapper;

    @Mock
    private ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    private OrdenIngresoLogisticaPersistenceAdapter adapter;

    // Objetos de prueba comunes
    private OrdenIngreso ordenIngreso;
    private DetalleOrdenIngreso detalle;
    private DetalleOrdenIngreso detalleConversionDiferente;
    private DetailsIngresoEntity detalleEntity;
    private KardexEntity kardexEntity;

    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con los mocks
        adapter = OrdenIngresoLogisticaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .kardexRepository(kardexRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .build();

        // Inicializar objetos comunes de prueba
        Articulo articulo = Articulo.builder()
                .id(289)
                .is_multiplo("1")
                .valor_conv(3) // Factor de conversión para pruebas
                .stock(BigDecimal.valueOf(100.000))
                .build();

        // Detalle sin conversión (idUnidad = idUnidadSalida)
        detalle = DetalleOrdenIngreso.builder()
                .articulo(articulo)
                .idUnidad(1)
                .idUnidadSalida(1) // Igual a idUnidad, sin conversión
                .cantidad(new BigDecimal("240.000"))
                .build();

        // Detalle con conversión (idUnidad ≠ idUnidadSalida)
        detalleConversionDiferente = DetalleOrdenIngreso.builder()
                .articulo(articulo)
                .idUnidad(1)
                .idUnidadSalida(6) // Diferente a idUnidad, se aplicará conversión
                .cantidad(BigDecimal.valueOf(240.000))
                .build();

        ordenIngreso = OrdenIngreso.builder()
                .id(1)
                .cod_ingreso("TEST-001")
                .motivo(Motivo.builder().idMotivo(4).descMotivo("COMPRAS").build())
                .fechaIngreso(LocalDate.of(2025, 3, 31))
                .almacen(Almacen.builder().idAlmacen(1).build())
                .build();

        detalleEntity = DetailsIngresoEntity.builder()
                .id(1L)
                .id_ordeningreso(1L)
                .id_articulo(289)
                .id_unidad(1)
                .cantidad(240.00)
                .costo_compra(2.15)
                .build();

        kardexEntity = KardexEntity.builder()
                .id_kardex(1L)
                .tipo_movimiento(1)
                .detalle("(TEST-001) - COMPRAS")
                .cantidad(BigDecimal.valueOf(240.00))
                .costo(BigDecimal.valueOf(2.15))
                .fecha_movimiento(LocalDate.of(2025, 3, 31))
                .build();
    }

    @Test
    void procesarDetalleGuardado_DeberiaGuardarKardex_CuandoDatosValidos() {
        // Arrange
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.just(kardexEntity));

        // Act
        Mono<DetalleOrdenIngreso> resultado = adapter.procesarDetalleGuardado(detalle, detalleEntity, ordenIngreso);

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result -> {
                    // Verificar que el detalle tenga el ID actualizado
                    return result.getId() == 1 &&
                            result.getArticulo().getId() == 289;
                })
                .verifyComplete();

        // Verify
        verify(kardexRepository).save(any(KardexEntity.class));
    }

    @Test
    void procesarDetalleGuardado_DeberiaRetornarError_CuandoFallaGuardadoKardex() {
        // Arrange
        R2dbcException dbException = mock(R2dbcException.class);
        when(dbException.getMessage()).thenReturn("Error de base de datos");
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.error(dbException));

        // Act
        Mono<DetalleOrdenIngreso> resultado = adapter.procesarDetalleGuardado(detalle, detalleEntity, ordenIngreso);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> {
                    // Verificar que se crea el error adecuado
                    return throwable instanceof RuntimeException &&
                            throwable.getMessage().contains("Error de base de datos al guardar el kardex");
                })
                .verify();

        // Verify
        verify(kardexRepository).save(any(KardexEntity.class));
    }

    @Test
    void procesarDetalleGuardado_DeberiaRetornarErrorGeneral_CuandoOcurreExcepcionNoEsperada() {
        // Arrange
        RuntimeException unexpectedException = new RuntimeException("Error inesperado");
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.error(unexpectedException));

        // Act
        Mono<DetalleOrdenIngreso> resultado = adapter.procesarDetalleGuardado(detalle, detalleEntity, ordenIngreso);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> {
                    // Verificar que se crea el error adecuado
                    return throwable instanceof RuntimeException &&
                            throwable.getMessage().contains("Error no esperado al guardar el kardex");
                })
                .verify();

        // Verify
        verify(kardexRepository).save(any(KardexEntity.class));
    }

    @Test
    void crearKardexEntity_DeberiaCrearKardexSinConversion_CuandoUnidadesIguales() {
        // Act
        KardexEntity resultado = adapter.crearKardexEntity(detalleEntity, detalle, ordenIngreso);

        // Assert
        // Comprobar que NO se aplicó conversión
        assertEquals(BigDecimal.valueOf(240.000000).setScale(6, RoundingMode.HALF_UP),
                resultado.getSaldoLote(),
                "La cantidad convertida debería ser igual a la cantidad original");

        assertEquals(BigDecimal.valueOf(340.000000).setScale(6, RoundingMode.HALF_UP),
                resultado.getSaldo_actual(),
                "El saldo actual debería ser la suma del stock y la cantidad sin conversión");

        // Verificar otros campos
        assertEquals("(TEST-001) - COMPRAS", resultado.getDetalle());
        assertEquals(BigDecimal.valueOf(240.00 * 2.15), resultado.getValorTotal());
        assertEquals(1, resultado.getTipo_movimiento());
    }

    @Test
    void manejarErroresGuardadoKardex_DeberiaRetornarErrorR2dbc_CuandoExcepcionEsR2dbc() {
        // Arrange
        R2dbcException dbException = mock(R2dbcException.class);
        when(dbException.getMessage()).thenReturn("Error de prueba");

        // Act
        Mono<KardexEntity> resultado = adapter.manejarErroresGuardadoKardex(dbException);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> {
                    return throwable instanceof RuntimeException &&
                            throwable.getMessage().contains("Error de base de datos al guardar el kardex");
                })
                .verify();
    }

    @Test
    void manejarErroresGuardadoKardex_DeberiaRetornarErrorGeneral_CuandoExcepcionNoEsR2dbc() {
        // Arrange
        RuntimeException generalException = new RuntimeException("Error general");

        // Act
        Mono<KardexEntity> resultado = adapter.manejarErroresGuardadoKardex(generalException);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> {
                    return throwable instanceof RuntimeException &&
                            throwable.getMessage().contains("Error no esperado al guardar el kardex");
                })
                .verify();
    }
}
