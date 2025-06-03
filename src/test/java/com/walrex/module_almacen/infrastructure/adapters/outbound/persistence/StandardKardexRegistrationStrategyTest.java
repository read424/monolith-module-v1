package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import io.r2dbc.spi.R2dbcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StandardKardexRegistrationStrategyTest {
    @Mock
    private KardexRepository kardexRepository;

    @Captor
    private ArgumentCaptor<KardexEntity> kardexCaptor;

    private StandardKardexRegistrationStrategy strategy;

    // Datos de prueba
    private OrdenIngreso ordenIngreso;
    private Motivo motivo;
    private Almacen almacen;
    private Articulo articulo;
    private DetalleOrdenIngreso detalle;
    private DetailsIngresoEntity detalleEntity;
    private ArticuloEntity articuloEntity;
    private MotivoEntity motivoEntity;
    private AlmacenEntity almacenEntity;

    @BeforeEach
    void setUp(){
        strategy = new StandardKardexRegistrationStrategy(kardexRepository);

        // Inicializar datos de prueba
        motivo = Motivo.builder()
            .descMotivo("COMPRA")
            .build();

        almacen = Almacen.builder()
            .idAlmacen(1)
            .build();

        articulo = Articulo.builder()
            .stock(BigDecimal.valueOf(100))
            .valor_conv(3)
            .build();

        ordenIngreso = OrdenIngreso.builder()
            .cod_ingreso("ALGI-I00013")
            .motivo(motivo)
            .fechaIngreso(LocalDate.of(2025, 3, 31))
            .almacen(almacen)
            .build();

        detalle = DetalleOrdenIngreso.builder()
            .articulo(articulo)
            .cantidad(BigDecimal.valueOf(10))
            .idUnidad(1)
            .idUnidadSalida(6)
            .build();

        detalleEntity = DetailsIngresoEntity.builder()
            .id(1L)
            .id_articulo(289)
            .id_unidad(1)
            .cantidad(10.00)
            .costo_compra(50.00)
            .build();
    }

    @Test
    void shouldRegisterKardexWithConversionWhenUnitsAreDifferent() {
        // Given
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.just(new KardexEntity()));

        // When
        Mono<Void> result = strategy.registrarKardex(detalleEntity, detalle, ordenIngreso);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(kardexRepository).save(kardexCaptor.capture());
        KardexEntity capturedKardex = kardexCaptor.getValue();

        // Verificar los valores calculados
        assertEquals(1, capturedKardex.getTipo_movimiento());
        assertEquals("(ALGI-I00013) - COMPRA", capturedKardex.getDetalle());
        assertEquals(BigDecimal.valueOf(10).doubleValue(), capturedKardex.getCantidad().doubleValue());
        assertEquals(BigDecimal.valueOf(50.0), capturedKardex.getCosto());
        assertEquals(BigDecimal.valueOf(500.0), capturedKardex.getValorTotal());

        // Verificar la conversión
        BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, 3)); // valor_conv = 2
        BigDecimal cantidadConvertida = BigDecimal.valueOf(10).multiply(factorConversion).setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalStock = cantidadConvertida.add(BigDecimal.valueOf(100)).setScale(6, RoundingMode.HALF_UP);

        assertEquals(totalStock, capturedKardex.getSaldo_actual());
        assertEquals(cantidadConvertida.setScale(6, RoundingMode.HALF_UP), capturedKardex.getSaldoLote());
        assertEquals(detalle.getIdUnidadSalida(), capturedKardex.getId_unidad_salida());
    }

    @Test
    void shouldRegisterKardexWithoutConversionWhenUnitsAreTheSame() {
        // Given
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.just(new KardexEntity()));

        // Configurar unidades iguales
        detalle.setIdUnidad(1);
        detalle.setIdUnidadSalida(1);

        // When
        Mono<Void> result = strategy.registrarKardex(detalleEntity, detalle, ordenIngreso);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(kardexRepository).save(kardexCaptor.capture());
        KardexEntity capturedKardex = kardexCaptor.getValue();

        // Verificar que no hay conversión cuando las unidades son iguales
        BigDecimal cantidadSinConversion = BigDecimal.valueOf(10);
        BigDecimal totalStock = cantidadSinConversion.add(BigDecimal.valueOf(100)).setScale(6, RoundingMode.HALF_UP);

        assertEquals(totalStock, capturedKardex.getSaldo_actual());
        assertEquals(cantidadSinConversion.setScale(6, RoundingMode.HALF_UP), capturedKardex.getSaldoLote());
        assertEquals(detalle.getIdUnidad(), capturedKardex.getId_unidad_salida());
    }

    @Test
    void shouldHandleDatabaseExceptionWhenSavingKardex() {
        // Given
        R2dbcException dbException = mock(R2dbcException.class);
        when(dbException.getMessage()).thenReturn("Database connection error");
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.error(dbException));

        // When
        Mono<Void> result = strategy.registrarKardex(detalleEntity, detalle, ordenIngreso);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Error de base de datos al guardar el kardex"))
                .verify();
    }

    @Test
    void shouldHandleGenericExceptionWhenSavingKardex() {
        // Given
        RuntimeException genericException = new RuntimeException("Generic error");
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(Mono.error(genericException));

        // When
        Mono<Void> result = strategy.registrarKardex(detalleEntity, detalle, ordenIngreso);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Error no esperado al guardar el kardex"))
                .verify();
    }
}