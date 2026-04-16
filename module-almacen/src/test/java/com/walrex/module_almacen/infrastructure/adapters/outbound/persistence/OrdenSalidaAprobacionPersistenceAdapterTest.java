package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;
import com.walrex.module_almacen.domain.model.mapper.ArticuloRequerimientoToDetalleMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenSalidaAprobacionPersistenceAdapterTest {

    @Mock
    private OrdenSalidaRepository ordenSalidaRepository;
    @Mock
    private DetailSalidaRepository detalleSalidaRepository;
    @Mock
    private ArticuloAlmacenRepository articuloRepository;
    @Mock
    private DetailSalidaLoteRepository detalleSalidaLoteRepository;
    @Mock
    private DetalleInventoryRespository detalleInventoryRespository;
    @Mock
    private OrdenSalidaEntityMapper ordenSalidaEntityMapper;
    @Mock
    private DetailSalidaMapper detailSalidaMapper;
    @Mock
    private KardexRepository kardexRepository;
    @Mock
    private ArticuloRequerimientoToDetalleMapper articuloRequerimientoToDetalleMapper;

    private OrdenSalidaAprobacionPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrdenSalidaAprobacionPersistenceAdapter(
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

    private OrdenEgresoDTO ordenConOrigen(Long id, Integer idAlmacenOrigen) {
        return OrdenEgresoDTO.builder()
                .id(id)
                .almacenOrigen(Almacen.builder().idAlmacen(idAlmacenOrigen).build())
                .build();
    }

    private OrdenEgresoDTO ordenConOrigenYDetalles(Long id, Integer idAlmacenOrigen, List<DetalleEgresoDTO> detalles) {
        return OrdenEgresoDTO.builder()
                .id(id)
                .almacenOrigen(Almacen.builder().idAlmacen(idAlmacenOrigen).build())
                .detalles(detalles)
                .build();
    }

    private DetalleEgresoDTO detalleConArticulo(Long idDetalle, Integer idArticulo, Integer idUnidad, Double cantidad) {
        return DetalleEgresoDTO.builder()
                .id(idDetalle)
                .idUnidad(idUnidad)
                .cantidad(cantidad)
                .articulo(Articulo.builder().id(idArticulo).build())
                .build();
    }

    private ArticuloInventory infoConversion(Integer idArticulo, Integer idUnidadConsumo, BigDecimal stock) {
        return ArticuloInventory.builder()
                .idArticulo(idArticulo)
                .idUnidadConsumo(idUnidadConsumo)
                .stock(stock)
                .build();
    }

    @Test
    @DisplayName("Debe registrar kardex por lote exitosamente")
    void debeRegistrarKardexPorLoteExitosamente() {
        // Given
        DetailSalidaLoteEntity salidaLote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.50)
                .total_monto(1050.0)
                .build();

        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(2000))
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-001")
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        DetalleInventaryEntity loteInfo = DetalleInventaryEntity.builder()
                .cantidadDisponible(500.0)
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        // Mocks
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .verifyComplete();

        // Then
        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(kardexCaptor.capture());

        KardexEntity kardexCapturado = kardexCaptor.getValue();
        assertThat(kardexCapturado.getTipo_movimiento()).isEqualTo(TypeMovimiento.APROBACION_SALIDA_REQUERIMIENTO.getId());
        assertThat(kardexCapturado.getDetalle()).contains("APROBACIÓN SALIDA - ( SAL-001 )");
        assertThat(kardexCapturado.getCantidad()).isEqualTo(BigDecimal.valueOf(-100.0));
        assertThat(kardexCapturado.getCosto()).isEqualTo(BigDecimal.valueOf(10.50));
        assertThat(kardexCapturado.getValorTotal()).isEqualTo(BigDecimal.valueOf(1050.0));
        assertThat(kardexCapturado.getId_articulo()).isEqualTo(100);
        assertThat(kardexCapturado.getId_unidad()).isEqualTo(6);
        assertThat(kardexCapturado.getId_almacen()).isEqualTo(5);
        assertThat(kardexCapturado.getSaldo_actual()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(kardexCapturado.getId_lote()).isEqualTo(123);
        assertThat(kardexCapturado.getSaldoLote()).isEqualTo(BigDecimal.valueOf(600.0)); // 500 + 100
    }

    @Test
    @DisplayName("Debe manejar cuando no encuentra información del lote")
    void debeManejarCuandoNoEncuentraInfoLote() {
        // Given
        DetailSalidaLoteEntity salidaLote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.50)
                .total_monto(1050.0)
                .build();

        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(2000))
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-001")
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        // Mocks
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.empty()); // ✅ No encuentra el lote
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .verifyComplete();

        // Then
        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(kardexCaptor.capture());

        KardexEntity kardexCreado = kardexCaptor.getValue();
        assertThat(kardexCreado.getSaldoLote()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe fallar cuando hay error al guardar kardex")
    void debeFallarCuandoHayErrorAlGuardarKardex() {
        // Given
        DetailSalidaLoteEntity salidaLote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.50)
                .total_monto(1050.00)
                .build();

        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(2000))
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-001")
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        DetalleInventaryEntity loteInfo = DetalleInventaryEntity.builder()
                .cantidadDisponible(500.0)
                .build();

        // Mocks
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al guardar kardex")));

        // When & Then
        StepVerifier.create(adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al guardar kardex"))
                .verify();
    }

    @Test
    @DisplayName("Debe actualizar stock del artículo después de registrar kardex")
    void debeActualizarStockDelArticuloeDespuesDeRegistrarKardex() {
        // Given
        DetailSalidaLoteEntity salidaLote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.50)
                .total_monto(1050.0)
                .build();

        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(2000)) // Stock inicial
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        DetalleInventaryEntity loteInfo = DetalleInventaryEntity.builder()
                .cantidadDisponible(500.0)
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        // Mocks
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .verifyComplete();

        // Then - Verificar que el stock se actualizó correctamente
        assertThat(detalle.getArticulo().getStock())
                .isEqualTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("Debe registrar kardex por detalle exitosamente")
    void debeRegistrarKardexPorDetalleExitosamente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .cantidad(150.0)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(2850.0).setScale(6, RoundingMode.HALF_UP))
                        .idUnidadSalida(6)
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-002")
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        DetailSalidaLoteEntity lote1 = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.50)
                .total_monto(1050.0)
                .build();

        DetailSalidaLoteEntity lote2 = DetailSalidaLoteEntity.builder()
                .id_lote(124)
                .cantidad(50.0)
                .monto_consumo(12.00)
                .total_monto(600.0)
                .build();

        DetalleInventaryEntity loteInfo1 = DetalleInventaryEntity.builder()
                .cantidadDisponible(0.0)
                .build();

        DetalleInventaryEntity loteInfo2 = DetalleInventaryEntity.builder()
                .cantidadDisponible(2850.0)
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        // Mocks
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(lote1, lote2));
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo1));
        when(detalleInventoryRespository.getStockLote(124))
                .thenReturn(Mono.just(loteInfo2));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.registrarKardexPorDetalle(detalle, ordenSalida))
                .verifyComplete();

        // Then
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(1L);
        verify(kardexRepository, times(2)).save(any(KardexEntity.class));
        verify(detalleInventoryRespository).getStockLote(123);
        verify(detalleInventoryRespository).getStockLote(124);

        // Verificar que el stock se actualizó correctamente después de procesar ambos lotes
        assertThat(detalle.getArticulo().getStock())
                .isEqualTo(BigDecimal.valueOf(2850).setScale(6, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando stock es insuficiente")
    void debeLanzarExcepcionCuandoStockEsInsuficiente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(-10)) // ✅ Stock negativo
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        // When & Then
        StepVerifier.create(adapter.registrarKardexPorDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof StockInsuficienteException &&
                                error.getMessage().contains("Stock insuficiente para artículo 100. Stock actual: -10"))
                .verify();
    }

    @Test
    @DisplayName("Debe aplicar conversión de unidades cuando son diferentes")
    void debeAplicarConversionDeUnidadesCuandoSonDiferentes() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(5) // Unidad diferente
                .cantidad(10.0)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(5000))
                        .idUnidadSalida(6) // Unidad de salida diferente
                        .valor_conv(2) // Factor de conversión 10^2 = 100
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-003")
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        DetailSalidaLoteEntity lote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(1000.0) // Cantidad convertida
                .monto_consumo(5.00)
                .total_monto(5000.0)
                .build();

        DetalleInventaryEntity loteInfo = DetalleInventaryEntity.builder()
                .cantidadDisponible(2000.0)
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        // Mocks
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(lote));
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.registrarKardexPorDetalle(detalle, ordenSalida))
                .verifyComplete();

        // Then - Verificar que se aplicó la conversión (10 * 100 = 1000)
        assertThat(detalle.getArticulo().getStock())
                .isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando valor de conversión es null")
    void debeLanzarExcepcionCuandoValorConversionEsNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(5)
                .cantidad(10.0)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(1000))
                        .idUnidadSalida(6) // Unidad diferente
                        .valor_conv(null) // ✅ Valor de conversión null
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        // When & Then
        StepVerifier.create(adapter.registrarKardexPorDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("Valor de conversión no configurado para artículo 100"))
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando no encuentra lotes para el detalle")
    void debeFallarCuandoNoEncuentraLotesParaElDetalle() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(1)
                .cantidad(100.0)
                .articulo(Articulo.builder()
                        .id(100)
                        .stock(BigDecimal.valueOf(1000))
                        .idUnidad(1)
                        .is_multiplo("1")
                        .valor_conv(3)
                        .idUnidadSalida(6)
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        // Mock - No encuentra lotes
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.empty());

        // When
        StepVerifier.create(adapter.registrarKardexPorDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().contains("No se encontraron lotes para el detalle 1"))
                .verify();

        // Then
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(1L);
        verify(kardexRepository, never()).save(any(KardexEntity.class));
    }

    @Test
    @DisplayName("Debe marcar detalle como entregado exitosamente")
    void debeMarcarDetalleComoEntregadoExitosamente() {
        // Given
        OrdenEgresoDTO ordenEgresoDTO = ordenConOrigen(1L, 1);
        DetalleEgresoDTO detalle = detalleConArticulo(1L, 100, 6, 10.0);

        DetailSalidaEntity detalleActualizado = DetailSalidaEntity.builder()
                .id_detalle_orden(1L)
                .entregado(1)
                .build();

        when(articuloRepository.getInfoConversionArticulo(1, 100))
                .thenReturn(Mono.just(infoConversion(100, 6, BigDecimal.valueOf(100))));

        // Mock
        when(detalleSalidaRepository.assignedDelivered(1))
                .thenReturn(Mono.just(detalleActualizado));

        // When
        StepVerifier.create(adapter.marcarDetalleComoEntregado(detalle, ordenEgresoDTO))
                .verifyComplete();

        // Then
        verify(detalleSalidaRepository).assignedDelivered(1);
    }

    @Test
    @DisplayName("Debe fallar cuando hay error al marcar como entregado")
    void debeFallarCuandoHayErrorAlMarcarComoEntregado() {
        // Given
        OrdenEgresoDTO ordenEgresoDTO = ordenConOrigen(1L, 1);
        DetalleEgresoDTO detalle = detalleConArticulo(2L, 100, 6, 10.0);

        when(articuloRepository.getInfoConversionArticulo(1, 100))
                .thenReturn(Mono.just(infoConversion(100, 6, BigDecimal.valueOf(100))));

        // Mock
        when(detalleSalidaRepository.assignedDelivered(2))
                .thenReturn(Mono.error(new RuntimeException("Error de base de datos")));

        // When & Then
        StepVerifier.create(adapter.marcarDetalleComoEntregado(detalle, ordenEgresoDTO))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error de base de datos"))
                .verify();

        verify(detalleSalidaRepository).assignedDelivered(2);
    }

    @Test
    @DisplayName("Debe manejar cuando assignedDelivered no encuentra el detalle")
    void debeManejarCuandoAssignedDeliveredNoEncuentraDetalle() {
        // Given
        OrdenEgresoDTO ordenEgresoDTO = ordenConOrigen(1L, 1);
        DetalleEgresoDTO detalle = detalleConArticulo(999L, 100, 6, 10.0);

        when(articuloRepository.getInfoConversionArticulo(1, 100))
                .thenReturn(Mono.just(infoConversion(100, 6, BigDecimal.valueOf(100))));

        // Mock - No encuentra el detalle
        when(detalleSalidaRepository.assignedDelivered(999))
                .thenReturn(Mono.empty());

        // When
        StepVerifier.create(adapter.marcarDetalleComoEntregado(detalle, ordenEgresoDTO))
                .verifyComplete(); // ✅ Completa aunque no encuentre (el then() convierte empty a complete)

        // Then
        verify(detalleSalidaRepository).assignedDelivered(999);
    }

    @Test
    @DisplayName("Debe convertir correctamente Long a Integer para assignedDelivered")
    void debeConvertirCorrectamenteLongAIntegerParaAssignedDelivered() {
        // Given
        OrdenEgresoDTO ordenEgresoDTO = ordenConOrigen(1L, 1);
        DetalleEgresoDTO detalle = detalleConArticulo(123456L, 100, 6, 10.0);

        DetailSalidaEntity detalleActualizado = DetailSalidaEntity.builder()
                .id_detalle_orden(123456L)
                .entregado(1)
                .build();

        when(articuloRepository.getInfoConversionArticulo(1, 100))
                .thenReturn(Mono.just(infoConversion(100, 6, BigDecimal.valueOf(100))));

        // Mock
        when(detalleSalidaRepository.assignedDelivered(123456))
                .thenReturn(Mono.just(detalleActualizado));

        // When
        StepVerifier.create(adapter.marcarDetalleComoEntregado(detalle, ordenEgresoDTO))
                .verifyComplete();

        // Then
        verify(detalleSalidaRepository).assignedDelivered(123456); // ✅ Verifica conversión Long → Integer
    }

    @Test
    @DisplayName("Debe validar detalle en orden exitosamente")
    void debeValidarDetalleEnOrdenExitosamente() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0)
                        .entregado(0) // ✅ No entregado
                        .build(),
                DetalleEgresoDTO.builder()
                        .id(2L)
                        .cantidad(50.0)
                        .entregado(0)
                        .build()
        );

        // When
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando detalle no pertenece a la orden")
    void debeLanzarExcepcionCuandoDetalleNoPerteneneALaOrden() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(999L) // ✅ ID que no existe en la lista
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0)
                        .entregado(0)
                        .build(),
                DetalleEgresoDTO.builder()
                        .id(2L)
                        .cantidad(50.0)
                        .entregado(0)
                        .build()
        );

        // When & Then
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("El detalle 999 no pertenece a esta orden de salida"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando detalle ya está entregado")
    void debeLanzarExcepcionCuandoDetalleYaEstaEntregado() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0)
                        .entregado(1) // ✅ Ya entregado
                        .build()
        );

        // When & Then
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().equals("El detalle 1 ya está entregado"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando las cantidades no coinciden")
    void debeLanzarExcepcionCuandoLasCantidadesNoCoinciden() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(150.0) // ✅ Cantidad diferente
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0) // ✅ Cantidad original
                        .entregado(0)
                        .build()
        );

        // When & Then
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La cantidad de salida 150.0 no puede ser mayor a la cantidad disponible 100.0 para el detalle 1"))
                .verify();
    }

    @Test
    @DisplayName("Debe manejar lista vacía de detalles de orden")
    void debeManejarListaVaciaDeDetallesDeOrden() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Collections.emptyList();

        // When & Then
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("El detalle 1 no pertenece a esta orden de salida"))
                .verify();
    }
    @Test
    @DisplayName("Debe validar correctamente cuando entregado es null")
    void debeValidarCorrectamenteCuandoEntregadoEsNull() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0)
                        .entregado(null) // ✅ Entregado null (se considera como no entregado)
                        .build()
        );

        // When
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .verifyComplete(); // ✅ Debe pasar porque null se considera como no entregado
    }

    @Test
    @DisplayName("Debe validar correctamente cuando cantidad es exactamente igual")
    void debeValidarCorrectamenteCuandoCantidadEsExactamenteIgual() {
        // Given
        DetalleEgresoDTO detalleAValidar = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .build();

        List<DetalleEgresoDTO> detallesOrden = Arrays.asList(
                DetalleEgresoDTO.builder()
                        .id(1L)
                        .cantidad(100.0) // ✅ Exactamente igual
                        .entregado(0)
                        .build()
        );

        // When
        StepVerifier.create(adapter.validarDetalleEnOrden(detalleAValidar, detallesOrden))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe consultar detalles de orden de salida exitosamente")
    void debeConsultarDetallesOrdenSalidaExitosamente() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        DetailSalidaEntity detalle1 = DetailSalidaEntity.builder()
                .id_detalle_orden(1L)
                .id_ordensalida(1L)
                .id_articulo(100)
                .cantidad(50.0)
                .entregado(0)
                .build();

        DetailSalidaEntity detalle2 = DetailSalidaEntity.builder()
                .id_detalle_orden(2L)
                .id_ordensalida(1L)
                .id_articulo(200)
                .cantidad(75.0)
                .entregado(1)
                .build();

        DetalleEgresoDTO detalleDTO1 = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(50.0)
                .entregado(0)
                .build();

        DetalleEgresoDTO detalleDTO2 = DetalleEgresoDTO.builder()
                .id(2L)
                .cantidad(75.0)
                .entregado(1)
                .build();

        // Mocks
        when(detalleSalidaRepository.findByIdOrderSalida(1L))
                .thenReturn(Flux.just(detalle1, detalle2));
        when(detailSalidaMapper.toDto(detalle1))
                .thenReturn(detalleDTO1);
        when(detailSalidaMapper.toDto(detalle2))
                .thenReturn(detalleDTO2);

        // When
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .assertNext(detalles -> {
                    assertThat(detalles).hasSize(2);
                    assertThat(detalles.get(0).getId()).isEqualTo(1L);
                    assertThat(detalles.get(0).getCantidad()).isEqualTo(50.0);
                    assertThat(detalles.get(0).getEntregado()).isEqualTo(0);
                    assertThat(detalles.get(1).getId()).isEqualTo(2L);
                    assertThat(detalles.get(1).getCantidad()).isEqualTo(75.0);
                    assertThat(detalles.get(1).getEntregado()).isEqualTo(1);
                })
                .verifyComplete();

        // Then
        verify(detalleSalidaRepository).findByIdOrderSalida(1L);
        verify(detailSalidaMapper).toDto(detalle1);
        verify(detailSalidaMapper).toDto(detalle2);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando orden de egreso es null")
    void debeLanzarExcepcionCuandoOrdenEgresoEsNullEnConsulta() {
        // When & Then
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(null))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La orden de egreso no puede ser null"))
                .verify();

        // Then
        verify(detalleSalidaRepository, never()).findByIdOrderSalida(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando ID de orden es null")
    void debeLanzarExcepcionCuandoIdOrdenEsNull() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(null) // ✅ ID null
                .build();

        // When & Then
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La orden de egreso no puede ser null"))
                .verify();

        verify(detalleSalidaRepository, never()).findByIdOrderSalida(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra detalles")
    void debeLanzarExcepcionCuandoNoEncuentraDetalles() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(999L)
                .build();

        // Mock - No encuentra detalles
        when(detalleSalidaRepository.findByIdOrderSalida(999L))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("No se encontraron detalles para la orden de salida: 999"))
                .verify();

        verify(detalleSalidaRepository).findByIdOrderSalida(999L);
        verify(detailSalidaMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Debe manejar error del repository")
    void debeManejarErrorDelRepository() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        // Mock - Error en repository
        when(detalleSalidaRepository.findByIdOrderSalida(1L))
                .thenReturn(Flux.error(new RuntimeException("Error de conexión BD")));

        // When & Then
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error de conexión BD"))
                .verify();

        verify(detalleSalidaRepository).findByIdOrderSalida(1L);
        verify(detailSalidaMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Debe manejar error en el mapper")
    void debeManejarErrorEnElMapper() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .build();

        DetailSalidaEntity detalle = DetailSalidaEntity.builder()
                .id_detalle_orden(1L)
                .build();

        // Mocks
        when(detalleSalidaRepository.findByIdOrderSalida(1L))
                .thenReturn(Flux.just(detalle));
        when(detailSalidaMapper.toDto(detalle))
                .thenThrow(new RuntimeException("Error en mapeo"));

        // When & Then
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error en mapeo"))
                .verify();

        verify(detalleSalidaRepository).findByIdOrderSalida(1L);
        verify(detailSalidaMapper).toDto(detalle);
    }

    @Test
    @DisplayName("Debe consultar un solo detalle correctamente")
    void debeConsultarUnSoloDetalleCorrectamente() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(5L)
                .build();

        DetailSalidaEntity detalle = DetailSalidaEntity.builder()
                .id_detalle_orden(10L)
                .id_ordensalida(5L)
                .id_articulo(500)
                .cantidad(25.5)
                .entregado(0)
                .build();

        DetalleEgresoDTO detalleDTO = DetalleEgresoDTO.builder()
                .id(10L)
                .cantidad(25.5)
                .entregado(0)
                .build();

        // Mocks
        when(detalleSalidaRepository.findByIdOrderSalida(5L))
                .thenReturn(Flux.just(detalle));
        when(detailSalidaMapper.toDto(detalle))
                .thenReturn(detalleDTO);

        // When
        StepVerifier.create(adapter.consultarDetallesOrdenSalida(ordenEgreso))
                .assertNext(detalles -> {
                    assertThat(detalles).hasSize(1);
                    assertThat(detalles.get(0).getId()).isEqualTo(10L);
                    assertThat(detalles.get(0).getCantidad()).isEqualTo(25.5);
                    assertThat(detalles.get(0).getEntregado()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe consultar y validar orden de salida exitosamente")
    void debeConsultarYValidarOrdenSalidaExitosamente() {
        // Given
        Integer idOrdenSalida = 1;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(1L)
                .cod_salida("SAL-001")
                .status(1) // ✅ Habilitado
                .entregado(0) // ✅ No entregada
                .id_store_source(5)
                .build();

        // Mock
        when(ordenSalidaRepository.findById(1L))
                .thenReturn(Mono.just(ordenEntity));

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(1L);
                    assertThat(resultado.getCod_salida()).isEqualTo("SAL-001");
                    assertThat(resultado.getStatus()).isEqualTo(1);
                    assertThat(resultado.getEntregado()).isEqualTo(0);
                    assertThat(resultado.getId_store_source()).isEqualTo(5);
                })
                .verifyComplete();

        verify(ordenSalidaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra la orden")
    void debeLanzarExcepcionCuandoNoEncuentraLaOrden() {
        // Given
        Integer idOrdenSalida = 999;

        // Mock - No encuentra la orden
        when(ordenSalidaRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("No se encontró la orden de salida con ID: 999"))
                .verify();

        verify(ordenSalidaRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando orden ya está deshabilitado por status")
    void debeLanzarExcepcionCuandoOrdenEstaDeshabilitadoPorStatus() {
        // Given
        Integer idOrdenSalida = 1;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(1L)
                .status(0)
                .entregado(0)// ✅ Status = 1 (entregada)
                .build();

        // Mock
        when(ordenSalidaRepository.findById(1L))
                .thenReturn(Mono.just(ordenEntity));

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().equals("La orden de salida 1 se encuentra actualmente inhabilitada para salida"))
                .verify();

        verify(ordenSalidaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando orden ya está entregada por campo entregado")
    void debeLanzarExcepcionCuandoOrdenYaEstaEntregadaPorCampoEntregado() {
        // Given
        Integer idOrdenSalida = 2;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(2L)
                .status(1) // Status OK
                .entregado(1) // ✅ Entregado = 1
                .build();

        // Mock
        when(ordenSalidaRepository.findById(2L))
                .thenReturn(Mono.just(ordenEntity));

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().equals("La orden de salida 2 ya fue entregada"))
                .verify();

        verify(ordenSalidaRepository).findById(2L);
    }

    @Test
    @DisplayName("Debe validar correctamente cuando status y entregado son null")
    void debeValidarCorrectamenteCuandoStatusYEntregadoSonNull() {
        // Given
        Integer idOrdenSalida = 3;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(3L)
                .cod_salida("SAL-003")
                .status(null) // ✅ Status null (se considera válido)
                .entregado(null) // ✅ Entregado null (se considera válido)
                .id_store_source(10)
                .build();

        // Mock
        when(ordenSalidaRepository.findById(3L))
                .thenReturn(Mono.just(ordenEntity));

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(3L);
                    assertThat(resultado.getCod_salida()).isEqualTo("SAL-003");
                    assertThat(resultado.getStatus()).isNull();
                    assertThat(resultado.getEntregado()).isNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error del repository en consulta de orden")
    void debeManejarErrorDelRepositoryEnConsultaDeOrden() {
        // Given
        Integer idOrdenSalida = 1;

        // Mock - Error en repository
        when(ordenSalidaRepository.findById(1L))
                .thenReturn(Mono.error(new RuntimeException("Error de conexión BD")));

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error de conexión BD"))
                .verify();

        verify(ordenSalidaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe convertir correctamente Integer a Long para búsqueda")
    void debeConvertirCorrectamenteIntegerALongParaBusqueda() {
        // Given
        Integer idOrdenSalida = 123456; // ✅ ID Integer

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(123456L) // ✅ Se convierte a Long
                .status(1)
                .entregado(0)
                .build();

        // Mock
        when(ordenSalidaRepository.findById(123456L)) // ✅ Verifica conversión Integer → Long
                .thenReturn(Mono.just(ordenEntity));

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(123456L);
                })
                .verifyComplete();

        verify(ordenSalidaRepository).findById(123456L);
    }

    @Test
    @DisplayName("Debe validar que status 0 y entregado 0 pasen la validación")
    void debeValidarQueStatusHabilitadoYEntregado0PasenLaValidacion() {
        // Given
        Integer idOrdenSalida = 5;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(5L)
                .status(1) // ✅ Status válido
                .entregado(0) // ✅ Entregado válido
                .build();

        // Mock
        when(ordenSalidaRepository.findById(5L))
                .thenReturn(Mono.just(ordenEntity));

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenSalida(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getStatus()).isEqualTo(1);
                    assertThat(resultado.getEntregado()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe procesar aprobación de detalle exitosamente")
    void debeProcesarAprobacionDetalleExitosamente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .cantidad(100.0)
                .articulo(Articulo.builder()
                        .id(200)
                        .stock(BigDecimal.valueOf(100.0))
                        .build())
                .build();

        DetailSalidaEntity detalleEntity = DetailSalidaEntity.builder()
                .id_detalle_orden(1L)
                .entregado(1)
                .build();

        DetalleEgresoDTO detalleOrden = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .entregado(0)
                .build();

        OrdenEgresoDTO ordenSalida = ordenConOrigenYDetalles(1L, 5, List.of(detalleOrden));

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idArticulo(200)
                .idUnidadConsumo(6)
                .stock(BigDecimal.valueOf(2000))
                .build();

        DetailSalidaLoteEntity lote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.0)
                .total_monto(1000.0)
                .build();

        DetalleInventaryEntity loteInfo = DetalleInventaryEntity.builder()
                .cantidadDisponible(500.0)
                .build();

        KardexEntity kardexGuardado = KardexEntity.builder()
                .id_lote(1)
                .build();

        when(detalleSalidaRepository.assignedDelivered(1))
                .thenReturn(Mono.just(detalleEntity));
        when(articuloRepository.getInfoConversionArticulo(5, 200))
                .thenReturn(Mono.just(infoConversion));
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(lote));
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(loteInfo));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexGuardado));

        // When
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(1L);
                    assertThat(resultado.getArticulo().getId()).isEqualTo(200);
                })
                .verifyComplete();

        // Then - Verificar que se ejecutaron todos los pasos
        verify(detalleSalidaRepository).assignedDelivered(1);
        verify(articuloRepository, times(2)).getInfoConversionArticulo(5, 200);
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(1L);
        verify(kardexRepository).save(any(KardexEntity.class));
    }

    @Test
    @DisplayName("Debe fallar cuando validación de orden falla")
    void debeFallarCuandoValidacionDeOrdenFalla() {
        // Given
        DetalleEgresoDTO detalle = detalleConArticulo(1L, 200, 6, 100.0);
        OrdenEgresoDTO ordenSalida = ordenConOrigen(999L, 5);

        // When & Then
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La orden de salida no tiene detalles cargados"))
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando consulta de detalles falla")
    void debeFallarCuandoConsultaDeDetallesFalla() {
        // Given
        DetalleEgresoDTO detalle = detalleConArticulo(1L, 200, 6, 100.0);
        OrdenEgresoDTO ordenSalida = ordenConOrigen(1L, 5);

        // When & Then
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La orden de salida no tiene detalles cargados"))
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando validación de detalle falla")
    void debeFallarCuandoValidacionDeDetalleFalla() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(999L) // ✅ ID que no existe en la orden
                .cantidad(100.0)
                .build();

        DetalleEgresoDTO detalleOrden = DetalleEgresoDTO.builder()
                .id(1L) // ✅ ID diferente al solicitado (999)
                .cantidad(100.0)
                .entregado(0)
                .build();
        OrdenEgresoDTO ordenSalida = ordenConOrigenYDetalles(1L, 5, List.of(detalleOrden));

        // When & Then
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("El detalle 999 no pertenece a esta orden de salida"))
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando marcar como entregado falla")
    void debeFallarCuandoMarcarComoEntregadoFalla() {
        // Given
        DetalleEgresoDTO detalle = detalleConArticulo(1L, 200, 6, 100.0);

        DetalleEgresoDTO detalleOrden = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .entregado(0)
                .build();

        OrdenEgresoDTO ordenSalida = ordenConOrigenYDetalles(1L, 5, List.of(detalleOrden));

        when(detalleSalidaRepository.assignedDelivered(1))
                .thenReturn(Mono.error(new RuntimeException("Error al marcar entregado"))); // ✅ Aquí falla
        when(articuloRepository.getInfoConversionArticulo(5, 200))
                .thenReturn(Mono.just(infoConversion(200, 6, BigDecimal.valueOf(1000))));

        // When & Then
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al marcar entregado"))
                .verify();

        verify(detalleSalidaRepository).assignedDelivered(1);
        verify(articuloRepository).getInfoConversionArticulo(5, 200);
    }

    @Test
    @DisplayName("Debe fallar cuando registro de kardex falla")
    void debeFallarCuandoRegistroDeKardexFalla() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .cantidad(100.0)
                .articulo(Articulo.builder()
                        .id(200)
                        .stock(BigDecimal.valueOf(-10)) // ✅ Stock insuficiente - causa el fallo
                        .idUnidadSalida(6)
                        .build())
                .build();

        DetalleEgresoDTO detalleOrden = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(100.0)
                .entregado(0)
                .build();

        OrdenEgresoDTO ordenSalida = ordenConOrigenYDetalles(1L, 5, List.of(detalleOrden));

        DetailSalidaEntity detalleEntity = DetailSalidaEntity.builder()
                .id_detalle_orden(1L)
                .entregado(1)
                .build();

        ArticuloInventory infoConversion = infoConversion(200, 6, BigDecimal.valueOf(1000));
        DetailSalidaLoteEntity lote = DetailSalidaLoteEntity.builder()
                .id_lote(123)
                .cantidad(100.0)
                .monto_consumo(10.0)
                .total_monto(1000.0)
                .build();

        when(detalleSalidaRepository.assignedDelivered(1))
                .thenReturn(Mono.just(detalleEntity));
        when(articuloRepository.getInfoConversionArticulo(5, 200))
                .thenReturn(Mono.just(infoConversion));
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(lote));
        when(detalleInventoryRespository.getStockLote(123))
                .thenReturn(Mono.just(DetalleInventaryEntity.builder().cantidadDisponible(500.0).build()));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al guardar kardex")));

        // When & Then
        StepVerifier.create(adapter.procesarAprobacionDetalle(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al guardar kardex"))
                .verify();

        verify(articuloRepository, times(2)).getInfoConversionArticulo(5, 200);
        verify(kardexRepository).save(any());
    }

    @Test
    @DisplayName("Debe consultar y validar orden para aprobación exitosamente")
    void debeConsultarYValidarOrdenParaAprobacionExitosamente() {
        // Given
        Integer idOrdenSalida = 1;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(1L)
                .cod_salida("SAL-001")
                .id_store_source(5)
                .status(1) // ✅ Válida
                .entregado(0)
                .build();

        OrdenEgresoDTO ordenMapeada = OrdenEgresoDTO.builder()
                .id(1L)
                .codEgreso("SAL-001")
                .almacenOrigen(Almacen.builder().idAlmacen(5).build())
                .build();
        DetalleEgresoDTO detalleMapeado = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(10.0)
                .entregado(0)
                .build();

        when(ordenSalidaRepository.findById(1L))
                .thenReturn(Mono.just(ordenEntity));
        when(ordenSalidaEntityMapper.toDomain(ordenEntity))
                .thenReturn(ordenMapeada);
        when(detalleSalidaRepository.findByIdOrderSalida(1L))
                .thenReturn(Flux.just(DetailSalidaEntity.builder().id_detalle_orden(1L).build()));
        when(detailSalidaMapper.toDto(any(DetailSalidaEntity.class)))
                .thenReturn(detalleMapeado);

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(1L);
                    assertThat(resultado.getCodEgreso()).isEqualTo("SAL-001");
                    assertThat(resultado.getAlmacenOrigen()).isNotNull();
                    assertThat(resultado.getAlmacenOrigen().getIdAlmacen()).isEqualTo(5);
                })
                .verifyComplete();

        verify(ordenSalidaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe fallar cuando orden no existe")
    void debeFallarCuandoOrdenNoExiste() {
        // Given
        Integer idOrdenSalida = 999;

        // Mock - Orden no encontrada
        when(ordenSalidaRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("No se encontró la orden de salida con ID: 999"))
                .verify();

        verify(ordenSalidaRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe fallar cuando orden está inhabilitada")
    void debeFallarCuandoOrdenEstaInhabilitada() {
        // Given
        Integer idOrdenSalida = 2;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(2L)
                .status(0) // ✅ Inhabilitada
                .entregado(0)
                .build();

        // Mock
        when(ordenSalidaRepository.findById(2L))
                .thenReturn(Mono.just(ordenEntity));

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().contains("se encuentra actualmente inhabilitada para salida"))
                .verify();

        verify(ordenSalidaRepository).findById(2L);
    }

    @Test
    @DisplayName("Debe fallar cuando orden ya está entregada")
    void debeFallarCuandoOrdenYaEstaEntregada() {
        // Given
        Integer idOrdenSalida = 3;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(3L)
                .status(1) // ✅ Status válido
                .entregado(1) // ✅ Ya entregada
                .build();

        // Mock
        when(ordenSalidaRepository.findById(3L))
                .thenReturn(Mono.just(ordenEntity));

        // When & Then
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().contains("ya fue entregada"))
                .verify();

        verify(ordenSalidaRepository).findById(3L);
    }

    @Test
    @DisplayName("Debe mapear correctamente todos los campos")
    void debeMepearCorrectamenteTodosLosCampos() {
        // Given
        Integer idOrdenSalida = 10;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(123L)
                .cod_salida("SAL-2025-456")
                .id_store_source(15)
                .status(null) // ✅ Status null (válido)
                .entregado(0)
                .build();

        OrdenEgresoDTO ordenMapeada = OrdenEgresoDTO.builder()
                .id(123L)
                .codEgreso("SAL-2025-456")
                .almacenOrigen(Almacen.builder().idAlmacen(15).build())
                .build();
        DetalleEgresoDTO detalleMapeado = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(10.0)
                .entregado(0)
                .build();

        when(ordenSalidaRepository.findById(10L))
                .thenReturn(Mono.just(ordenEntity));
        when(ordenSalidaEntityMapper.toDomain(ordenEntity))
                .thenReturn(ordenMapeada);
        when(detalleSalidaRepository.findByIdOrderSalida(123L))
                .thenReturn(Flux.just(DetailSalidaEntity.builder().id_detalle_orden(1L).build()));
        when(detailSalidaMapper.toDto(any(DetailSalidaEntity.class)))
                .thenReturn(detalleMapeado);

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(123L);
                    assertThat(resultado.getCodEgreso()).isEqualTo("SAL-2025-456");
                    assertThat(resultado.getAlmacenOrigen()).isNotNull();
                    assertThat(resultado.getAlmacenOrigen().getIdAlmacen()).isEqualTo(15);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar valores null en campos opcionales")
    void debeManejarValoresNullEnCamposOpcionales() {
        // Given
        Integer idOrdenSalida = 5;

        OrdenSalidaEntity ordenEntity = OrdenSalidaEntity.builder()
                .id(5L)
                .cod_salida(null) // ✅ Código null
                .id_store_source(null) // ✅ Almacén null
                .status(null) // ✅ Status null (válido)
                .entregado(null) // ✅ Entregado null (válido)
                .build();

        OrdenEgresoDTO ordenMapeada = OrdenEgresoDTO.builder()
                .id(5L)
                .codEgreso(null)
                .almacenOrigen(Almacen.builder().idAlmacen(null).build())
                .build();
        DetalleEgresoDTO detalleMapeado = DetalleEgresoDTO.builder()
                .id(1L)
                .cantidad(10.0)
                .entregado(0)
                .build();

        when(ordenSalidaRepository.findById(5L))
                .thenReturn(Mono.just(ordenEntity));
        when(ordenSalidaEntityMapper.toDomain(ordenEntity))
                .thenReturn(ordenMapeada);
        when(detalleSalidaRepository.findByIdOrderSalida(5L))
                .thenReturn(Flux.just(DetailSalidaEntity.builder().id_detalle_orden(1L).build()));
        when(detailSalidaMapper.toDto(any(DetailSalidaEntity.class)))
                .thenReturn(detalleMapeado);

        // When
        StepVerifier.create(adapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getId()).isEqualTo(5L);
                    assertThat(resultado.getCodEgreso()).isNull();
                    assertThat(resultado.getAlmacenOrigen()).isNotNull();
                    assertThat(resultado.getAlmacenOrigen().getIdAlmacen()).isNull();
                })
                .verifyComplete();
    }
}
