package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.domain.model.mapper.DetEgresoLoteEntityToItemKardexMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenSalidaTransformacionPersistenceAdapterTest {
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
    private KardexRegistrationStrategy kardexStratgey;
    @Mock
    private DetEgresoLoteEntityToItemKardexMapper detEgresoLoteEntityToItemKardexMapper;

    private OrdenSalidaTransformacionPersistenceAdapter adapter;

    // Datos de prueba
    private Articulo articulo;
    private DetailSalidaLoteEntity salidaLote;
    private DetalleEgresoDTO detalle;
    private OrdenEgresoDTO ordenSalida;
    private InventoryEntity inventoryEntity;
    private DetalleInventaryEntity detalleInventoryEntity;
    private KardexEntity kardexEntity;

    @BeforeEach
    void setUp() {
        adapter = new OrdenSalidaTransformacionPersistenceAdapter(
                ordenSalidaRepository,
                detalleSalidaRepository,
                articuloRepository,
                detalleSalidaLoteRepository,
                detalleInventoryRespository,
                ordenSalidaEntityMapper,
                detailSalidaMapper,
                kardexStratgey,
                detEgresoLoteEntityToItemKardexMapper
        );

        setupTestData();
    }
    /*
    @Test
    void deberiaRegistrarKardexPorLoteExitosamente() {
        // Given
        detalle.getArticulo().setStock(BigDecimal.valueOf(120000.00));
        when(detalleInventoryRespository.getStockLote(37830))
                .thenReturn(Mono.just(detalleInventoryEntity));
        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexEntity));

        // When
        Mono<Void> resultado = adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete();

        // Verificar que se consultaron los saldos
        verify(detalleInventoryRespository).getStockLote(37830);

        // Verificar que se guardó el kardex con valores correctos
        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(kardexCaptor.capture());

        KardexEntity capturedKardex = kardexCaptor.getValue();

        assertEquals(TypeMovimiento.INTERNO_TRANSFORMACION.getId(), capturedKardex.getTipo_movimiento()); // Salida
        assertEquals(BigDecimal.valueOf(-60000.0), capturedKardex.getCantidad()); // Cantidad negativa
        assertEquals(BigDecimal.valueOf(120000.0), capturedKardex.getSaldo_actual()); //
        assertEquals(BigDecimal.valueOf(120000.0), capturedKardex.getSaldoLote()); //
    }

    @Test
    void deberiaManejarLoteNoEncontrado() {
        // Given
        detalle.getArticulo().setStock(BigDecimal.valueOf(120000.00));

        when(detalleInventoryRespository.getStockLote(37830))
                .thenReturn(Mono.empty()); // Lote no encontrado

        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexEntity));

        // When
        Mono<Void> resultado = adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete();

        // Verificar que usó saldo ZERO por defecto para lote
        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(kardexCaptor.capture());

        KardexEntity capturedKardex = kardexCaptor.getValue();
        assertEquals(BigDecimal.ZERO, capturedKardex.getSaldoLote()); // Saldo lote = 0
    }

    @Test
    void deberiaManejarErrorEnKardexRepository() {
        // Given
        detalle.getArticulo().setStock(BigDecimal.valueOf(120000.00));

        when(detalleInventoryRespository.getStockLote(37830))
                .thenReturn(Mono.just(detalleInventoryEntity));

        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Error de base de datos en kardex")));

        // When
        Mono<Void> resultado = adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                "Error de base de datos en kardex".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void deberiaCalcularCorrectamenteSaldoLoteAntesDelTrigger() {
        // Given - El lote tenía 120000, salieron 60000, quedaron 60000
        detalle.getArticulo().setStock(BigDecimal.valueOf(120000.00));

        when(detalleInventoryRespository.getStockLote(37830))
                .thenReturn(Mono.just(detalleInventoryEntity)); // cantidadDisponible = 60000 (después del trigger)

        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(kardexEntity));

        // When
        Mono<Void> resultado = adapter.registrarKardexPorLote(salidaLote, detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete();

        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(kardexCaptor.capture());

        KardexEntity capturedKardex = kardexCaptor.getValue();

        // Verificar que calculó correctamente el saldo antes del trigger
        // cantidadDisponible (60000) + cantidad salida (60000) = saldo antes (120000)
        assertEquals(BigDecimal.valueOf(120000.0000), capturedKardex.getSaldoLote());
    }

    @Test
    void deberiaRegistrarKardexPorDetalleConMultiplesLotes() {
        // Given
        // Configurar artículo con stock inicial
        articulo.setIdUnidad(1);
        articulo.setIdUnidadSalida(6); // Misma unidad, sin conversión
        articulo.setIs_multiplo("1");
        articulo.setValor_conv(3);
        articulo.setStock(BigDecimal.valueOf(20000.0)); // Stock post trigger

        // ✅ Crear dos lotes que suman la cantidad total
        DetailSalidaLoteEntity lote1 = DetailSalidaLoteEntity.builder()
                .id_salida_lote(1L)
                .id_detalle_orden(1275834)
                .id_lote(100)
                .cantidad(20000.0) // Primer lote: 20 kg
                .monto_consumo(0.0035)
                .total_monto(70.0)
                .build();

        DetailSalidaLoteEntity lote2 = DetailSalidaLoteEntity.builder()
                .id_salida_lote(2L)
                .id_detalle_orden(1275834)
                .id_lote(200)
                .cantidad(40000.0) // Segundo lote: 40 kg
                .monto_consumo(0.0035)
                .total_monto(140.0)
                .build();

        // ✅ Configurar entidades de inventario para cada lote
        DetalleInventaryEntity inventarioLote1 = DetalleInventaryEntity.builder()
                .idLote(100L)
                .cantidadDisponible(0.0) // Ya descontado por trigger
                .build();

        DetalleInventaryEntity inventarioLote2 = DetalleInventaryEntity.builder()
                .idLote(200L)
                .cantidadDisponible(20000.0) // Ya descontado por trigger
                .build();

        // Mock repository responses
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1275834L))
                .thenReturn(Flux.just(lote1, lote2)); // ✅ Retorna ambos lotes

        when(detalleInventoryRespository.getStockLote(100))
                .thenReturn(Mono.just(inventarioLote1));

        when(detalleInventoryRespository.getStockLote(200))
                .thenReturn(Mono.just(inventarioLote2));

        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(new KardexEntity()));

        // When
        Mono<Void> resultado = adapter.registrarKardexPorDetalle(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete();

        // ✅ Verificar que se procesaron ambos lotes SECUENCIALMENTE
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(anyLong());
        verify(detalleInventoryRespository).getStockLote(100);
        verify(detalleInventoryRespository).getStockLote(200);

        // ✅ Verificar que se guardaron 2 registros de kardex
        verify(kardexRepository, times(2)).save(any(KardexEntity.class));

        // ✅ Capturar los kardex guardados para verificar el orden y valores
        ArgumentCaptor<KardexEntity> kardexCaptor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository, times(2)).save(kardexCaptor.capture());

        List<KardexEntity> kardexGuardados = kardexCaptor.getAllValues();

        // ✅ Verificar PRIMER kardex (Lote 1)
        KardexEntity kardexLote1 = kardexGuardados.get(0);
        assertEquals(BigDecimal.valueOf(-20000.0), kardexLote1.getCantidad());
        assertEquals(BigDecimal.valueOf(80000.0).setScale(6, RoundingMode.HALF_UP), kardexLote1.getSaldo_actual()); // Stock inicial: 20000 + 60000 = 80000
        assertEquals(BigDecimal.valueOf(20000.0), kardexLote1.getSaldoLote()); // 20000 disponible antes del trigger

        // ✅ Verificar SEGUNDO kardex (Lote 2) - procesado después del primero
        KardexEntity kardexLote2 = kardexGuardados.get(1);
        assertEquals(BigDecimal.valueOf(-40000.0), kardexLote2.getCantidad());
        assertEquals(BigDecimal.valueOf(60000.0).setScale(6, RoundingMode.HALF_UP), kardexLote2.getSaldo_actual()); // 80000 - 20000 = 60000
        assertEquals(BigDecimal.valueOf(60000.0), kardexLote2.getSaldoLote()); // 60000 disponible antes del trigger

        // ✅ Verificar stock final del artículo
        assertEquals(BigDecimal.valueOf(20000.0).setScale(6, RoundingMode.HALF_UP), detalle.getArticulo().getStock()); //
    }

    @Test
    void deberiaManejarStockNegativo() {
        // Given
        articulo.setStock(BigDecimal.valueOf(-1000.0)); // ✅ Stock negativo

        // When
        Mono<Void> resultado = adapter.registrarKardexPorDetalle(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof StockInsuficienteException &&
                                throwable.getMessage().contains("Stock insuficiente para artículo 617") &&
                                throwable.getMessage().contains("Stock actual: -1000.0"))
                .verify();

        // Verificar que NO se procesaron lotes
        verify(detalleSalidaLoteRepository, never()).findByIdDetalleOrden(anyLong());
        verify(kardexRepository, never()).save(any(KardexEntity.class));
    }

    @Test
    void deberiaPermitirStockCero() {
        // Given
        articulo.setStock(BigDecimal.ZERO); // ✅ Stock en cero (válido)
        articulo.setIdUnidadSalida(6);
        articulo.setIs_multiplo("1");
        articulo.setValor_conv(3);

        when(detalleSalidaLoteRepository.findByIdDetalleOrden(anyLong()))
                .thenReturn(Flux.empty()); // Sin lotes para simplificar

        // When
        Mono<Void> resultado = adapter.registrarKardexPorDetalle(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete(); // ✅ No debe fallar con stock = 0
    }

    @Test
    void deberiaManejarValorConversionNull() {
        // Given
        articulo.setStock(BigDecimal.valueOf(1000.0));
        articulo.setIdUnidad(1);
        articulo.setIdUnidadSalida(2); // ✅ Unidad diferente, necesita conversión
        articulo.setValor_conv(null); // ✅ Valor de conversión null

        // When
        Mono<Void> resultado = adapter.registrarKardexPorDetalle(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Valor de conversión no configurado para artículo 617"))
                .verify();
    }

    @Test
    void deberiaRegistrarKardexConLotesExitosamente() {
        // Given
        // Configurar múltiples detalles en la orden
        DetalleEgresoDTO detalle1 = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()
                        .id(100)
                        .idUnidadSalida(1)
                        .valor_conv(0)
                        .stock(BigDecimal.valueOf(5000.0))
                        .build())
                .idUnidad(1)
                .cantidad(10.0)
                .build();

        DetalleEgresoDTO detalle2 = DetalleEgresoDTO.builder()
                .id(2L)
                .articulo(Articulo.builder()
                        .id(200)
                        .idUnidadSalida(1)
                        .valor_conv(0)
                        .stock(BigDecimal.valueOf(3000.0))
                        .build())
                .idUnidad(1)
                .cantidad(20.0)
                .build();

        ordenSalida.setDetalles(List.of(detalle1, detalle2));

        // Mock para el primer detalle
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(
                        DetailSalidaLoteEntity.builder()
                                .id_lote(100)
                                .cantidad(10.0)
                                .monto_consumo(1.00)
                                .total_monto(10.00)
                                .build()
                ));

        // Mock para el segundo detalle
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(2L))
                .thenReturn(Flux.just(
                        DetailSalidaLoteEntity.builder()
                                .id_lote(200)
                                .cantidad(20.0)
                                .monto_consumo(2.00)
                                .total_monto(40.00)
                                .build()
                ));

        // Mock para inventarios
        when(detalleInventoryRespository.getStockLote(100))
                .thenReturn(Mono.just(DetalleInventaryEntity.builder()
                        .cantidadDisponible(50.0)
                        .build()));

        when(detalleInventoryRespository.getStockLote(200))
                .thenReturn(Mono.just(DetalleInventaryEntity.builder()
                        .cantidadDisponible(80.0)
                        .build()));

        when(kardexRepository.save(any(KardexEntity.class)))
                .thenReturn(Mono.just(new KardexEntity()));

        // When
        Mono<OrdenEgresoDTO> resultado = adapter.registrarKardexConLotes(ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenSalida) // ✅ Debe retornar la misma orden
                .verifyComplete();

        // Verificar que se procesaron ambos detalles
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(1L);
        verify(detalleSalidaLoteRepository).findByIdDetalleOrden(2L);

        // Verificar que se guardaron kardex para ambos detalles
        verify(kardexRepository, times(2)).save(any(KardexEntity.class));
    }

    @Test
    void deberiaRegistrarKardexConLotesSinDetalles() {
        // Given
        ordenSalida.setDetalles(Collections.emptyList()); // ✅ Sin detalles

        // When
        Mono<OrdenEgresoDTO> resultado = adapter.registrarKardexConLotes(ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenSalida) // ✅ Debe retornar la orden sin procesar nada
                .verifyComplete();

        // Verificar que NO se procesó nada
        verify(detalleSalidaLoteRepository, never()).findByIdDetalleOrden(anyLong());
        verify(kardexRepository, never()).save(any(KardexEntity.class));
    }

    @Test
    void deberiaManejarErrorEnBusquedaDeLotes() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()
                        .id(100)
                        .idUnidadSalida(1)
                        .valor_conv(0)
                        .stock(BigDecimal.valueOf(5000.0)) // ✅ Stock válido
                        .build())
                .idUnidad(1)
                .cantidad(10.0)
                .build();

        ordenSalida.setDetalles(List.of(detalle));

        // ✅ Mock que cause error en getStockLote
        when(detalleSalidaLoteRepository.findByIdDetalleOrden(1L))
                .thenReturn(Flux.just(
                        DetailSalidaLoteEntity.builder()
                                .id_lote(100) // ✅ ID válido
                                .cantidad(10.0)
                                .build()
                ));

        when(detalleInventoryRespository.getStockLote(100))
                .thenReturn(Mono.error(new RuntimeException("Error en consulta de lote")));

        // When
        Mono<OrdenEgresoDTO> resultado = adapter.registrarKardexConLotes(ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                "Error en consulta de lote".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void deberiaAplicarConversionCuandoUnidadesSonDiferentes() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(1) // Unidad original
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        ArticuloEntity infoConversion = ArticuloEntity.builder()
                .idUnidadConsumo(6) // ✅ Unidad diferente
                .isMultiplo("1")
                .valorConv(3)
                .stock(BigDecimal.valueOf(5000.0))
                .build();

        // When
        Mono<DetalleEgresoDTO> resultado = adapter.aplicarConversion(detalle, infoConversion);

        // Then
        StepVerifier.create(resultado)
                .assertNext(detalleActualizado -> {
                    // ✅ Verificar que se aplicó la conversión
                    assertEquals(Integer.valueOf(6), detalleActualizado.getArticulo().getIdUnidadSalida());
                    assertEquals("1", detalleActualizado.getArticulo().getIs_multiplo());
                    assertEquals(Integer.valueOf(3), detalleActualizado.getArticulo().getValor_conv());
                    assertEquals(BigDecimal.valueOf(5000.0), detalleActualizado.getArticulo().getStock());
                })
                .verifyComplete();
    }

    @Test
    void noDeberiaAplicarConversionCuandoUnidadesSonIguales() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(1) // Unidad original
                .articulo(Articulo.builder()
                        .id(100)
                        .idUnidadSalida(1) // Inicialmente null
                        .is_multiplo("0")
                        .valor_conv(0)
                        .stock(BigDecimal.valueOf(3000.0))
                        .build())
                .build();

        ArticuloEntity infoConversion = ArticuloEntity.builder()
                .idUnidadConsumo(1) // ✅ Misma unidad
                .isMultiplo("0")
                .valorConv(0)
                .stock(BigDecimal.valueOf(3000.0))
                .build();

        // When
        Mono<DetalleEgresoDTO> resultado = adapter.aplicarConversion(detalle, infoConversion);

        // Then
        StepVerifier.create(resultado)
                .assertNext(detalleActualizado -> {
                    // ✅ Solo se debe actualizar idUnidadSalida, lo demás se mantiene
                    assertEquals(Integer.valueOf(1), detalleActualizado.getArticulo().getIdUnidadSalida());

                    // ✅ Valores originales NO deben cambiar
                    assertEquals("0", detalleActualizado.getArticulo().getIs_multiplo()); // Se mantiene original
                    assertEquals(Integer.valueOf(0), detalleActualizado.getArticulo().getValor_conv()); // Se mantiene original
                    assertEquals(BigDecimal.valueOf(3000.0), detalleActualizado.getArticulo().getStock()); // Se mantiene original
                })
                .verifyComplete();
    }

    @Test
    void deberiaManejarIdUnidadNullEnDetalle() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(123L)
                .idUnidad(null) // ✅ ID de unidad null
                .articulo(Articulo.builder()
                        .id(456)
                        .build())
                .build();

        ArticuloEntity infoConversion = ArticuloEntity.builder()
                .idUnidadConsumo(2)
                .build();

        // When
        Mono<DetalleEgresoDTO> resultado = adapter.aplicarConversion(detalle, infoConversion);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("ID de unidad no puede ser null para el detalle 123") &&
                                throwable.getMessage().contains("artículo 456"))
                .verify();
    }
     */

    @Test
    void deberiaBuscarInfoConversionExitosamente() {

        ArticuloEntity infoConversion = ArticuloEntity.builder()
                .idArticulo(617)
                .idUnidadConsumo(6)
                .isMultiplo("1")
                .valorConv(3)
                .stock(BigDecimal.valueOf(1000.0))
                .build();

        when(articuloRepository.getInfoConversionArticulo(1, 617))
                .thenReturn(Mono.just(infoConversion));

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectNext(infoConversion)
                .verifyComplete();

        verify(articuloRepository).getInfoConversionArticulo(1, 617);
    }

    @Test
    void deberiaManejarCuandoNoSeEncuentraInfoConversion() {

        when(articuloRepository.getInfoConversionArticulo(1, 617))
                .thenReturn(Mono.empty()); // ✅ No se encuentra

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                                throwable.getMessage().contains("No se encontró información de conversión para el artículo: 617"))
                .verify();
    }

    @Test
    void deberiaManejarDetalleNull() {
        // Given
        OrdenEgresoDTO orden = OrdenEgresoDTO.builder().build();
        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(null, orden);
        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                "El detalle no puede ser null".equals(throwable.getMessage()))
                .verify();

        verify(articuloRepository, never()).getInfoConversionArticulo(anyInt(), anyInt());
    }

    @Test
    void deberiaManejarOrdenNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder().build();

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, null);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                "La orden de egreso no puede ser null".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void deberiaManejarAlmacenOrigenNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .articulo(Articulo.builder().id(456).build())
                .build();

        OrdenEgresoDTO orden = OrdenEgresoDTO.builder()
                .id(789L)
                .almacenOrigen(null) // ✅ Almacén null
                .build();

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, orden);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Almacén origen no puede ser null para la orden 789"))
                .verify();
    }

    @Test
    void deberiaManejarIdAlmacenNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .articulo(Articulo.builder().id(456).build())
                .build();

        OrdenEgresoDTO orden = OrdenEgresoDTO.builder()
                .id(789L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(null) // ✅ ID almacén null
                        .build())
                .build();

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, orden);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("ID de almacén origen no puede ser null para la orden 789"))
                .verify();
    }

    @Test
    void deberiaManejarArticuloNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(123L)
                .articulo(null) // ✅ Artículo null
                .build();

        OrdenEgresoDTO orden = OrdenEgresoDTO.builder()
                .almacenOrigen(Almacen.builder().idAlmacen(1).build())
                .build();

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, orden);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Artículo no puede ser null para el detalle 123"))
                .verify();
    }

    @Test
    void deberiaManejarIdArticuloNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(123L)
                .articulo(Articulo.builder()
                        .id(null) // ✅ ID artículo null
                        .build())
                .build();

        OrdenEgresoDTO orden = OrdenEgresoDTO.builder()
                .almacenOrigen(Almacen.builder().idAlmacen(1).build())
                .build();

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, orden);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("ID de artículo no puede ser null para el detalle 123"))
                .verify();
    }


    @Test
    void deberiaManejarErrorEnRepositorio() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .articulo(Articulo.builder().id(456).build())
                .build();

        OrdenEgresoDTO orden = OrdenEgresoDTO.builder()
                .almacenOrigen(Almacen.builder().idAlmacen(1).build())
                .build();

        when(articuloRepository.getInfoConversionArticulo(1, 456))
                .thenReturn(Mono.error(new RuntimeException("Error de base de datos")));

        // When
        Mono<ArticuloEntity> resultado = adapter.buscarInfoConversion(detalle, orden);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                "Error de base de datos".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void deberiaProcessarEntregaYConversionExitosamente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(123L)
                .idUnidad(1)
                .articulo(Articulo.builder()
                        .id(456)
                        .stock(BigDecimal.valueOf(2000.0))
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(789L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(1)
                        .build())
                .build();

        // Mock de assignedDelivered
        DetailSalidaEntity detalleActualizado = new DetailSalidaEntity();
        when(detalleSalidaRepository.assignedDelivered(123))
                .thenReturn(Mono.just(detalleActualizado));

        // Mock de buscarInfoConversion
        ArticuloEntity infoConversion = ArticuloEntity.builder()
                .idUnidadConsumo(6) // Unidad diferente para aplicar conversión
                .isMultiplo("1")
                .valorConv(3)
                .stock(BigDecimal.valueOf(5000.0))
                .build();

        when(articuloRepository.getInfoConversionArticulo(1, 456))
                .thenReturn(Mono.just(infoConversion));

        // When
        Mono<DetalleEgresoDTO> resultado = adapter.procesarEntregaYConversion(detalle, ordenSalida);

        // Then
        StepVerifier.create(resultado)
                .assertNext(detalleResultado -> {
                    // ✅ Verificar que se aplicó la conversión
                    assertEquals(Integer.valueOf(6), detalleResultado.getArticulo().getIdUnidadSalida());
                    assertEquals("1", detalleResultado.getArticulo().getIs_multiplo());
                    assertEquals(Integer.valueOf(3), detalleResultado.getArticulo().getValor_conv());
                    assertEquals(BigDecimal.valueOf(5000.0), detalleResultado.getArticulo().getStock());

                    // ✅ Verificar que es la misma instancia (modificada)
                    assertSame(detalle, detalleResultado);
                })
                .verifyComplete();

        // ✅ Verificar que se ejecutaron los métodos en orden correcto
        InOrder inOrder = inOrder(detalleSalidaRepository, articuloRepository);
        inOrder.verify(detalleSalidaRepository).assignedDelivered(123);
        inOrder.verify(articuloRepository).getInfoConversionArticulo(1, 456);

    }

    private void setupTestData() {
        // Configurar DetalleEgresoDTO
        articulo = Articulo.builder()
                .id(617)
                .codigo("PRO00093")
                .descripcion("FOAMASTER 405")
                .build();

        Almacen almacenOrigen = Almacen.builder()
                .idAlmacen(1)
                .build();

        Almacen almacenDestino = Almacen.builder()
                .idAlmacen(1)
                .build();

        // Configurar DetailSalidaLoteEntity
        salidaLote = DetailSalidaLoteEntity.builder()
                .id_salida_lote(799670L)
                .id_detalle_orden(1275834)
                .id_lote(37830)
                .cantidad(60000.0000)
                .monto_consumo(0.0035)
                .total_monto(210.00)
                .id_ordensalida(386498)
                .build();

        detalle = DetalleEgresoDTO.builder()
                .id(1275834L)
                .idOrdenEgreso(386498L)
                .articulo(articulo)
                .idUnidad(1)
                .cantidad(60.0)
                .precio(3.5)
                .totalMonto(BigDecimal.valueOf(210.0))
                .build();

        // Configurar OrdenEgresoDTO
        ordenSalida = OrdenEgresoDTO.builder()
                .id(386498L)
                .codEgreso("ALGI-S26429")
                .almacenOrigen(almacenOrigen)
                .almacenDestino(almacenDestino)
                .build();

        // Configurar InventoryEntity (stock actual: 100)
        inventoryEntity = InventoryEntity.builder()
                .idArticulo(100)
                .stock(BigDecimal.valueOf(100.0))
                .idAlmacen(1)
                .build();

        // Configurar DetalleInventaryEntity (saldo lote: 50) aqui ya esta restado la cantidad que acaba de salir
        detalleInventoryEntity = DetalleInventaryEntity.builder()
                .idLote(37830L)
                .idArticulo(617)
                .cantidad(120000.0)
                .cantidadDisponible(60000.0)
                .costoCompra(BigDecimal.valueOf(3.50))
                .costoConsumo(BigDecimal.valueOf(0.0035))
                .build();

        // Configurar KardexEntity para respuesta
        kardexEntity = KardexEntity.builder()
                .id_kardex(1L)
                .tipo_movimiento(3)
                .detalle("SALIDA TRANFORMACIÓN - ( ALGI-S26429 )")
                .cantidad(BigDecimal.valueOf(-60.00))
                .costo(BigDecimal.valueOf(0.0035))
                .valorTotal(BigDecimal.valueOf(210))
                .fecha_movimiento(LocalDate.now())
                .id_articulo(articulo.getId())
                .id_unidad(detalle.getIdUnidad())
                .id_unidad_salida(detalle.getArticulo().getIdUnidadSalida())
                .id_almacen(ordenSalida.getAlmacenOrigen().getIdAlmacen())
                .saldo_actual(BigDecimal.valueOf(120000.00))
                .id_documento(ordenSalida.getId().intValue())
                .id_lote(salidaLote.getId_lote())
                .id_detalle_documento(1275834)
                .saldoLote(BigDecimal.valueOf(120000.00))
                .build();
    }
}
