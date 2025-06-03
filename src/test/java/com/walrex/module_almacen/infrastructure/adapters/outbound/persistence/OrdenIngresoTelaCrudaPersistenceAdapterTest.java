package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenIngresoTelaCrudaPersistenceAdapterTest {

    @Mock
    private OrdenIngresoRepository ordenIngresoRepository;

    @Mock
    private ArticuloAlmacenRepository articuloRepository;

    @Mock
    private DetailsIngresoRepository detalleRepository;

    @Mock
    private KardexRepository kardexRepository;

    @Mock
    private DetalleRolloRepository detalleRolloRepository;

    @Mock
    private OrdenIngresoEntityMapper mapper;

    @Mock
    private ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    private OrdenIngresoTelaCrudaPersistenceAdapter adapter;

    // Objetos de prueba comunes
    private OrdenIngreso ordenIngreso;
    private DetalleOrdenIngreso detalleSinRollos;
    private DetalleOrdenIngreso detalleConRollos;
    private DetailsIngresoEntity detalleEntity;
    private DetalleRolloEntity rolloEntity1;
    private DetalleRolloEntity rolloEntity2;

    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con los mocks
        adapter = OrdenIngresoTelaCrudaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .detalleRolloRepository(detalleRolloRepository)
                .build();
        // Inicializar objetos comunes de prueba
        Articulo articulo = Articulo.builder()
                .id(289)
                .is_multiplo("0")
                .valor_conv(1)
                .stock(BigDecimal.valueOf(100.00))
                .build();

        // Detalle sin rollos
        detalleSinRollos = DetalleOrdenIngreso.builder()
                .id(1)
                .articulo(articulo)
                .idUnidad(1)
                .idUnidadSalida(1)
                .cantidad(BigDecimal.valueOf(240.00))
                .detallesRollos(Collections.emptyList()) // Lista vacía
                .build();
        // Crear rollos para la prueba
        DetalleRollo rollo1 = DetalleRollo.builder()
                .codRollo("ROLLO-001")
                .pesoRollo(new BigDecimal("120.00"))
                .build();

        DetalleRollo rollo2 = DetalleRollo.builder()
                .codRollo("ROLLO-002")
                .pesoRollo(new BigDecimal("120.00"))
                .build();

        // Detalle con rollos
        detalleConRollos = DetalleOrdenIngreso.builder()
                .id(1)
                .articulo(articulo)
                .idUnidad(1)
                .idUnidadSalida(1)
                .cantidad(new BigDecimal("240.00"))
                .detallesRollos(Arrays.asList(rollo1, rollo2))
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

        rolloEntity1 = DetalleRolloEntity.builder()
                .id(1)
                .codRollo("ROLLO-001")
                .pesoRollo(new BigDecimal("120.00"))
                .idDetOrdenIngreso(1)
                .ordenIngreso(1)
                .build();

        rolloEntity2 = DetalleRolloEntity.builder()
                .id(2)
                .codRollo("ROLLO-002")
                .pesoRollo(new BigDecimal("120.00"))
                .idDetOrdenIngreso(1)
                .ordenIngreso(1)
                .build();
    }

    @Test
    void procesarDetalleGuardado_DeberiaRetornarError_CuandoNoHayRollos() {
        // Act & Assert
        StepVerifier.create(adapter.procesarDetalleGuardado(detalleSinRollos, detalleEntity, ordenIngreso))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                                ((ResponseStatusException) throwable).getReason().contains("al menos un rollo")
                )
                .verify();

        // Verify
        verifyNoInteractions(detalleRolloRepository);
    }

    @Test
    void procesarDetalleGuardado_DeberiaGuardarRollos_CuandoHayRollos() {
        // Arrange
        DetalleRolloEntity rolloEntity1 = DetalleRolloEntity.builder()
                .id(1)
                .ordenIngreso(1)
                .idDetOrdenIngreso(1)
                .codRollo("0221-001")
                .pesoRollo(BigDecimal.valueOf(22.10))
                .build();

        DetalleRolloEntity rolloEntity2 = DetalleRolloEntity.builder()
                .id(2)
                .ordenIngreso(1)
                .idDetOrdenIngreso(1)
                .codRollo("0221-002")
                .pesoRollo(BigDecimal.valueOf(20.00))
                .build();

        // Crea una lista de resultados
        List<Mono<DetalleRolloEntity>> responses = Arrays.asList(
                Mono.just(rolloEntity1),
                Mono.just(rolloEntity2)
        );

        // Configura el mock para devolver elementos de la lista en secuencia
        when(detalleRolloRepository.save(any(DetalleRolloEntity.class)))
                .thenAnswer(AdditionalAnswers.returnsElementsOf(responses));

        // Act & Assert
        StepVerifier.create(adapter.procesarDetalleGuardado(detalleConRollos, detalleEntity, ordenIngreso))
                .expectNextMatches(result ->
                        result.getId() == 1 &&
                                result.getDetallesRollos() != null &&
                                result.getDetallesRollos().size() == 2 &&
                                result.getDetallesRollos().get(0).getId() == 1 &&
                                result.getDetallesRollos().get(1).getId() == 2
                )
                .verifyComplete();

        // Verify
        verify(detalleRolloRepository, times(2)).save(any(DetalleRolloEntity.class));
    }

    @Test
    void procesarDetalleGuardado_DeberiaRetornarError_CuandoFallaGuardadoRollos() {
        // Arrange
        RuntimeException expectedError = new RuntimeException("Error al guardar rollo");
        when(detalleRolloRepository.save(any())).thenReturn(Mono.error(expectedError));

        // Act & Assert
        StepVerifier.create(adapter.procesarDetalleGuardado(detalleConRollos, detalleEntity, ordenIngreso))
                .expectError(RuntimeException.class)
                .verify();

        // Verify
        verify(detalleRolloRepository).save(any());
    }
}
