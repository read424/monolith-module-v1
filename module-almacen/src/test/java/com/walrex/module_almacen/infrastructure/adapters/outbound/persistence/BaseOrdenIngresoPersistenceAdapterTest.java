package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.common.Exception.OrdenIngresoException;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseOrdenIngresoPersistenceAdapterTest {
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

    // Implementación de prueba de la clase abstracta
    private TestOrdenIngresoPersistenceAdapter adapter;

    // Objetos de prueba comunes
    private OrdenIngreso ordenIngreso;
    private DetalleOrdenIngreso detalle;

    private OrdenIngresoEntity ordenIngresoEntity;
    private DetailsIngresoEntity detalleEntity;
    private ArticuloInventory articuloEntity;

    // Orden de ingreso base para las pruebas
    private OrdenIngreso crearOrdenIngresoBase() {
        return OrdenIngreso.builder()
                .idCliente(86)
                .motivo(Motivo.builder().idMotivo(4).descMotivo("COMPRAS").build())
                .comprobante(1)
                .codSerie("F001")
                .nroComprobante("1120")
                .fechaIngreso(LocalDate.parse("2025-03-31"))
                .fechaComprobante(LocalDate.parse("2025-03-31"))
                .observacion("")
                .almacen(Almacen.builder().idAlmacen(1).build())
                .idOrdenCompra(13232)
                .detalles(Collections.emptyList())
                .build();
    }

    private OrdenIngresoEntity crearOrdenIngresoEntity(){
        return OrdenIngresoEntity.builder()
                .id_ordeningreso(1L)
                .id_cliente(86)
                .id_motivo(4)
                .id_comprobante(1)
                .nu_serie("F001")
                .nu_comprobante("1120")
                .fec_ingreso(LocalDate.of(2025, 3, 31))
                .fec_referencia(LocalDate.of(2025,3,31))
                .observacion("")
                .id_almacen(1)
                .id_comprobante(13232)
                .build();
    }

    private DetailsIngresoEntity crearDetalleProducto(){
        return DetailsIngresoEntity.builder()
                .id_articulo(289)
                .lote("001120-1")
                .id_unidad(1)
                .cantidad(240.00)
                .costo_compra(2.15)
                .id_moneda(2)
                .build();
    }

    // Detalle de orden de prueba
    private DetalleOrdenIngreso crearDetalleOrdenIngreso() {
        return DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                    .id(289)
                    .build()
                )
                .idUnidad(1)
                .lote("001120-1")
                .cantidad(BigDecimal.valueOf(240.0000))
                .idTipoProducto(1)
                .costo(BigDecimal.valueOf(2.15))
                .idMoneda(2)
                .build();
    }


    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con los mocks
        adapter = TestOrdenIngresoPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .build();

        // Inicializar objetos comunes de prueba
        Articulo articulo = Articulo.builder()
                .id(289)
                .build();

        detalle = DetalleOrdenIngreso.builder()
                .articulo(articulo)
                .idUnidad(1)
                .lote("001120-1")
                .cantidad(BigDecimal.valueOf(240.0000))
                .idTipoProducto(1)
                .costo(BigDecimal.valueOf(2.15))
                .idMoneda(2)
                .build();

        articuloEntity = ArticuloInventory.builder()
                .idArticulo(289)
                .idUnidad(1)
                .idUnidadConsumo(6)
                .valorConv(3)
                .isMultiplo("1")
                .stock(BigDecimal.valueOf(100.00))
                .build();

        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        detalles.add(detalle);

        ordenIngreso = crearOrdenIngresoBase();

        ordenIngresoEntity = crearOrdenIngresoEntity();

        detalleEntity = crearDetalleProducto();
    }

    @Test
    void guardarOrdenIngresoLogistica_DeberiaRetornarError_CuandoNoHayDetalles() {
        // Arrange
        OrdenIngreso ordenSinDetalles = crearOrdenIngresoBase();

        // Act & Assert
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenSinDetalles))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                                ((ResponseStatusException) throwable).getReason().contains("debe tener al menos un detalle")
                )
                .verify();

        // Verify
        verifyNoInteractions(ordenIngresoRepository);
    }

    @Test
    void guardarOrdenIngresoLogistica_DeberiaGuardarOrdenYDetalles_CuandoDatosValidos() {
        // Arrange
        detalle = crearDetalleOrdenIngreso();
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        detalles.add(detalle);
        ordenIngreso.setId(1);
        ordenIngreso.setDetalles(detalles);
        detalleEntity.setId(1L);

        // Configurar comportamiento para ArticuloRepository.getInfoConversionArticulo
        when(articuloRepository.getInfoConversionArticulo(anyInt(), anyInt()))
                .thenReturn(Mono.just(articuloEntity));
        when(mapper.toEntity(any(OrdenIngreso.class))).thenReturn(ordenIngresoEntity);
        when(ordenIngresoRepository.save(any(OrdenIngresoEntity.class))).thenReturn(Mono.just(ordenIngresoEntity));
        when(mapper.toDomain(any(OrdenIngresoEntity.class))).thenReturn(ordenIngreso);

        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.just(detalleEntity));

        // Mock procesarDetalleGuardado
        adapter.setReturnDetalle(detalle);

        // Act & Assert
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectNextMatches(result ->
                        result.getId() == 1 &&
                                result.getDetalles().size() == 1 &&
                                result.getDetalles().get(0).getArticulo().getId() == 289
                )
                .verifyComplete();

        // Verify
        verify(mapper).toEntity(any(OrdenIngreso.class));
        verify(ordenIngresoRepository).save(any(OrdenIngresoEntity.class));
        verify(mapper).toDomain(any(OrdenIngresoEntity.class));
        verify(articuloIngresoLogisticaMapper).toEntity(any(DetalleOrdenIngreso.class));
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));
    }

    @Test
    void guardarOrdenIngresoLogistica_DeberiaManejarErrorR2dbc_CuandoRepositorioFalla() {
        // Arrange
        detalle = crearDetalleOrdenIngreso();
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        detalles.add(detalle);
        ordenIngreso.setDetalles(detalles);

        when(mapper.toEntity(any(OrdenIngreso.class))).thenReturn(ordenIngresoEntity);
        R2dbcDataIntegrityViolationException dbException = mock(R2dbcDataIntegrityViolationException.class);
        when(dbException.getMessage()).thenReturn("Error de prueba");
        when(ordenIngresoRepository.save(any(OrdenIngresoEntity.class))).thenReturn(Mono.error(dbException));

        // Act & Assert
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectErrorMatches(throwable ->
                        throwable instanceof OrdenIngresoException &&
                                throwable.getMessage().contains("Error de integridad de datos")
                )
                .verify();

        // Verify
        verify(ordenIngresoRepository).save(any(OrdenIngresoEntity.class));
        verifyNoInteractions(detalleRepository);
    }

    @Test
    void guardarOrdenIngresoLogistica_DeberiaManejarErrorGeneral_CuandoOcurreExcepcion() {
        // Arrange
        detalle = crearDetalleOrdenIngreso();
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        detalles.add(detalle);
        ordenIngreso.setDetalles(detalles);

        when(mapper.toEntity(any(OrdenIngreso.class))).thenReturn(ordenIngresoEntity);
        RuntimeException generalException = new RuntimeException("Error general");
        when(ordenIngresoRepository.save(any(OrdenIngresoEntity.class))).thenReturn(Mono.error(generalException));

        // Act & Assert
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectErrorMatches(throwable ->
                        throwable instanceof OrdenIngresoException &&
                                throwable.getMessage().contains("Error no esperado")
                )
                .verify();

        // Verify
        verify(ordenIngresoRepository).save(any(OrdenIngresoEntity.class));
        verifyNoInteractions(detalleRepository);
    }

    @Test
    void procesarDetalle_DeberiaAplicarConversion_CuandoIdUnidadSalidaEsNulo() {
        // Arrange
        ordenIngreso.setId(1);
        detalleEntity.setId(1L);

        DetalleOrdenIngreso detalleConversionNull = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder().id(289).build())
                .idUnidad(1)
                .idUnidadSalida(null)
                .cantidad(BigDecimal.valueOf(240.000))
                .build();

        when(articuloRepository.getInfoConversionArticulo(anyInt(), anyInt()))
                .thenReturn(Mono.just(articuloEntity));

        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.just(detalleEntity));

        adapter.setReturnDetalle(detalleConversionNull);

        // Invocar directamente el método privado a través de la clase de prueba
        Mono<DetalleOrdenIngreso> resultado = adapter.testProcesarDetalle(detalleConversionNull, ordenIngreso);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result ->{
                    // Aunque no se aplique conversión, idUnidadSalida debería establecerse igual a idUnidad
                    return result.getIdUnidadSalida() == 6 && // Debe tomar el idUnidadConsumo de articuloEntity
                            "1".equals(result.getArticulo().getIs_multiplo()) &&
                            result.getArticulo().getValor_conv() == 3;
                })
                .verifyComplete();

        // Verify
        verify(articuloRepository).getInfoConversionArticulo(anyInt(), anyInt());
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));
    }

    @Test
    void procesarDetalle_DeberiaUsarIdUnidadSalidaExistente_CuandoNoEsNulo() {
        // Arrange
        ordenIngreso.setId(1);
        detalleEntity.setId(1L);

        // Crear un detalle con idUnidadSalida ya definido (no nulo)
        DetalleOrdenIngreso detalleConIdUnidadSalida = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(289)
                        .is_multiplo("1")
                        .valor_conv(1)
                        .stock(BigDecimal.valueOf(100.0))
                        .build())
                .idUnidad(1)
                .idUnidadSalida(3) // Valor no nulo, ya definido
                .cantidad(BigDecimal.valueOf(240.000))
                .build();

        // Configurar comportamiento para guardarDetalleOrdenIngreso
        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.just(detalleEntity));

        // Configurar qué devuelve procesarDetalleGuardado
        adapter.setReturnDetalle(detalleConIdUnidadSalida);

        // Invocar directamente el método privado
        Mono<DetalleOrdenIngreso> resultado = adapter.testProcesarDetalle(detalleConIdUnidadSalida, ordenIngreso);

        // Act & Assert con información de depuración
        StepVerifier.create(resultado)
                .consumeNextWith(result -> {
                    // Validaciones
                    assertEquals(3, result.getIdUnidadSalida(),
                            "idUnidadSalida debería mantener su valor original");
                    assertEquals(BigDecimal.valueOf(240.000).setScale(3), result.getCantidad().setScale(3),
                            "La cantidad debería mantenerse igual");
                })
                .verifyComplete();

        // Verificar que se llamaron los métodos esperados
        verify(articuloIngresoLogisticaMapper).toEntity(any(DetalleOrdenIngreso.class));
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));

        // Verificar que NO se llamó a buscarInfoConversion (usando articuloRepository)
        verifyNoInteractions(articuloRepository);
    }

    @Test
    void buscarInfoConversion_DeberiaRetornarError_CuandoNoHayInformacion() {
        // Arrange
        when(articuloRepository.getInfoConversionArticulo(anyInt(), anyInt()))
                .thenReturn(Mono.empty());

        // Invocar directamente el método privado
        Mono<ArticuloInventory> resultado = adapter.testBuscarInfoConversion(detalle, ordenIngreso);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                                ((ResponseStatusException) throwable).getReason().contains("No se encontró información de conversión")
                )
                .verify();

        // Verify
        verify(articuloRepository).getInfoConversionArticulo(anyInt(), anyInt());
    }

    @Test
    void aplicarConversion_DeberiaEstablecerValores_CuandoUnidadesDiferentes() {
        // Arrange
        DetalleOrdenIngreso detalleTest = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder().id(1).build())
                .idUnidad(1) // Diferente a idUnidadConsumo (2)
                .build();

        // Invocar directamente el método privado
        Mono<DetalleOrdenIngreso> resultado = adapter.testAplicarConversion(detalleTest, articuloEntity);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result ->
                        result.getIdUnidadSalida() == 6 &&
                                "1".equals(result.getArticulo().getIs_multiplo()) &&
                                result.getArticulo().getValor_conv() == 3 &&
                                result.getArticulo().getStock().compareTo(BigDecimal.valueOf(100.000)) == 0
                )
                .verifyComplete();
    }

    @Test
    void aplicarConversion_DeberiaUsarMismaUnidad_CuandoUnidadesIguales() {
        // Arrange
        DetalleOrdenIngreso detalleTest = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder().id(289).build())
                .idUnidad(1) //
                .build();

        // Invocar directamente el método privado
        Mono<DetalleOrdenIngreso> resultado = adapter.testAplicarConversion(detalleTest, articuloEntity);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result ->
                        result.getIdUnidadSalida() == 6 && // Debe ser igual a idUnidad
                                result.getArticulo().getId() == 289
                )
                .verifyComplete();
    }

    @Test
    void guardarDetalleOrdenIngreso_DeberiaGuardarDetalle_CuandoDatosValidos() {
        // Arrange
        ordenIngreso.setId(1);
        detalleEntity.setId(1L);
        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.just(detalleEntity));

        adapter.setReturnDetalle(detalle);

        // Invocar directamente el método privado
        Mono<DetalleOrdenIngreso> resultado = adapter.testGuardarDetalleOrdenIngreso(detalle, ordenIngreso);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result -> {
                    System.out.println();
                    return result.getArticulo().getId() == 289 &&
                            result.getIdUnidad() == 1;
                })
                .verifyComplete();

        // Verify
        verify(articuloIngresoLogisticaMapper).toEntity(any(DetalleOrdenIngreso.class));
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));
    }

    @Test
    void guardarDetalleOrdenIngreso_DeberiaManejarError_CuandoRepositorioFalla() {
        // Arrange
        ordenIngreso.setId(1);
        when(articuloIngresoLogisticaMapper.toEntity(any(DetalleOrdenIngreso.class))).thenReturn(detalleEntity);

        RuntimeException dbException = new RuntimeException("Error al guardar detalle");
        when(detalleRepository.save(any(DetailsIngresoEntity.class))).thenReturn(Mono.error(dbException));

        // Invocar directamente el método privado
        Mono<DetalleOrdenIngreso> resultado = adapter.testGuardarDetalleOrdenIngreso(detalle, ordenIngreso);

        // Act & Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof OrdenIngresoException &&
                                throwable.getMessage().contains("Error al registrar detalle")
                )
                .verify();

        // Verify
        verify(articuloIngresoLogisticaMapper).toEntity(any(DetalleOrdenIngreso.class));
        verify(detalleRepository).save(any(DetailsIngresoEntity.class));
    }

    @Test
    void actualizarIdDetalle_DeberiaRetornarDetalleConIdActualizado() {
        // Arrange & Act
        detalleEntity.setId(1L);
        Mono<DetalleOrdenIngreso> resultado = adapter.actualizarIdDetalle(detalle, detalleEntity);

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(result ->
                        result.getId() == 1 && // ID del detalleEntity
                                result.getArticulo().getId() == 289
                )
                .verifyComplete();
    }

    // Clase interna para pruebas que implementa la clase abstracta
    @SuperBuilder
    private static class TestOrdenIngresoPersistenceAdapter extends BaseOrdenIngresoPersistenceAdapter {
        private DetalleOrdenIngreso returnDetalle;

        public void setReturnDetalle(DetalleOrdenIngreso detalle) {
            this.returnDetalle = detalle;
        }

        // Métodos para acceder a métodos privados de la clase base
        public Mono<DetalleOrdenIngreso> testProcesarDetalle(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return procesarDetalle(detalle, ordenIngreso);
        }

        public Mono<ArticuloInventory> testBuscarInfoConversion(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return buscarInfoConversion(detalle, ordenIngreso);
        }

        public Mono<DetalleOrdenIngreso> testAplicarConversion(DetalleOrdenIngreso detalle, ArticuloInventory infoConversion) {
            return aplicarConversion(detalle, infoConversion);
        }

        public Mono<DetalleOrdenIngreso> testGuardarDetalleOrdenIngreso(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return guardarDetalleOrdenIngreso(detalle, ordenIngreso);
        }

        @Override
        protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
                DetalleOrdenIngreso detalle,
                DetailsIngresoEntity savedDetalleEntity,
                OrdenIngreso ordenIngreso) {
            // Implementación simple para pruebas
            detalle.setId(savedDetalleEntity.getId().intValue());
            return Mono.just(returnDetalle != null ? returnDetalle : detalle);
        }
    }
}
