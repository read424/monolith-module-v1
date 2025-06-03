package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgregarOrdenIngresoLogisticaServiceTest {
    @Mock
    private OrdenIngresoAdapterFactory adapterFactory;

    @Mock
    private OrdenIngresoLogisticaPort logisticaAdapter;

    @Mock
    private OrdenIngresoLogisticaPort telaCrudaAdapter;

    @InjectMocks
    private AgregarOrdenIngresoLogisticaService service;

    private OrdenIngreso ordenIngresoSinRollos;
    private OrdenIngreso ordenIngresoConRollos;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        ordenIngresoSinRollos = OrdenIngreso.builder()
                .idCliente(86)
                .motivo(Motivo.builder()
                        .idMotivo(4)
                        .descMotivo("COMPRAS")
                        .build()
                )
                .almacen(Almacen.builder()
                        .idAlmacen(1)
                        .build()
                )
                .fechaIngreso(LocalDate.of(2025, 5, 12))
                .comprobante(1)
                .codSerie("F001")
                .nroComprobante("1181")
                .detalles(crearDetallesSinRollos())
                .build();

        ordenIngresoConRollos = OrdenIngreso.builder()
                .idCliente(53)
                .motivo(Motivo.builder()
                        .idMotivo(1)
                        .descMotivo("INGRESO")
                        .build()
                )
                .almacen(Almacen.builder()
                        .idAlmacen(2)
                        .build()
                )
                .fechaIngreso(LocalDate.of(2025,5,19))
                .comprobante(5)
                .codSerie("001")
                .nroComprobante("108")
                .idOrigen(2)
                .detalles(crearDetallesConRollos())
                .build();
    }

    @Test
    void deberiaUsarAdaptadorLogisticaParaOrdenSinRollos() {
        // Given
        when(adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL))
                .thenReturn(Mono.just(logisticaAdapter));
        when(logisticaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngresoSinRollos));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngresoSinRollos);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngresoSinRollos)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL);
        verify(logisticaAdapter).guardarOrdenIngresoLogistica(ordenIngresoSinRollos);
        verifyNoInteractions(telaCrudaAdapter);
    }

    @Test
    void deberiaUsarAdaptadorTelaCrudaParaOrdenConRollos() {
        // Given
        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.just(telaCrudaAdapter));
        when(telaCrudaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngresoConRollos));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngresoConRollos);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngresoConRollos)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.TELA_CRUDA);
        verify(telaCrudaAdapter).guardarOrdenIngresoLogistica(ordenIngresoConRollos);
        verifyNoInteractions(logisticaAdapter);
    }

    @Test
    void deberiaUsarAdaptadorLogisticaParaOrdenSinDetalles() {
        // Given
        ordenIngresoSinRollos.setDetalles(null);

        when(adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL))
                .thenReturn(Mono.just(logisticaAdapter));
        when(logisticaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngresoSinRollos));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngresoSinRollos);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngresoSinRollos)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL);
        verify(logisticaAdapter).guardarOrdenIngresoLogistica(ordenIngresoSinRollos);
    }

    @Test
    void deberiaUsarAdaptadorLogisticaParaOrdenConDetallesVacios() {
        // Given
        ordenIngresoSinRollos.setDetalles(Collections.emptyList());

        when(adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL))
                .thenReturn(Mono.just(logisticaAdapter));
        when(logisticaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngresoSinRollos));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngresoSinRollos);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngresoSinRollos)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL);
        verify(logisticaAdapter).guardarOrdenIngresoLogistica(ordenIngresoSinRollos);
    }

    @Test
    void deberiaUsarAdaptadorLogisticaParaOrdenConDetallesSinRollosNulos() {
        // Given
        OrdenIngreso ordenSinRollosNulos = OrdenIngreso.builder()
                .idCliente(53)
                .motivo(Motivo.builder()
                        .idMotivo(1)
                        .descMotivo("INGRESO")
                        .build()
                )
                .almacen(Almacen.builder()
                        .idAlmacen(2)
                        .build()
                )
                .fechaIngreso(LocalDate.of(2025,5,19))
                .comprobante(5)
                .codSerie("001")
                .nroComprobante("108")
                .idOrigen(2)
                .build();
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(7768)
                        .codigo("REVER00001")
                        .descripcion("REVERSIBLE POLYCOTTON")
                        .idUnidad(1)
                        .build())
                .lote("108-1")
                .idUnidad(1)
                .cantidad(BigDecimal.valueOf(2))
                .detallesRollos(null)
                .build();

        detalles.add(detalle);

        ordenSinRollosNulos.setDetalles(detalles);

        when(adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL))
                .thenReturn(Mono.just(logisticaAdapter));
        when(logisticaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenSinRollosNulos));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenSinRollosNulos);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenSinRollosNulos)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL);
        verify(logisticaAdapter).guardarOrdenIngresoLogistica(ordenSinRollosNulos);
    }

    @Test
    void deberiaManejarErrorDeAdaptador() {
        // Given
        RuntimeException expectedException = new RuntimeException("Error en el adaptador");

        when(adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL))
                .thenReturn(Mono.just(logisticaAdapter));
        when(logisticaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.error(expectedException));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngresoSinRollos);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        "Error en el adaptador".equals(error.getMessage()))
                .verify();
    }

    // Métodos auxiliares para crear los datos de prueba
    private List<DetalleOrdenIngreso> crearDetallesSinRollos() {
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(289)
                        .build())
                .idUnidad(1)
                .cantidad(BigDecimal.valueOf(120))
                .costo(BigDecimal.valueOf(2.15))
                .excentoImp(false)
                .idMoneda(2)
                .build();
        detalle.setDetallesRollos(Collections.emptyList());
        detalles.add(detalle);
        return detalles;
    }

    private List<DetalleOrdenIngreso> crearDetallesConRollos() {
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(7768)
                        .codigo("REVER00001")
                        .descripcion("REVERSIBLE POLYCOTTON")
                        .idUnidad(1)
                        .build())
                .lote("108-1")
                .idUnidad(1)
                .cantidad(BigDecimal.valueOf(2))
                .build();

        // Crear algunos detalles de rollos
        List<DetalleRollo> rollos = new ArrayList<>();
        DetalleRollo rollo0 = DetalleRollo.builder()
                .codRollo("108-2-1")
                .pesoRollo(BigDecimal.valueOf(20.58))
                .build();
        rollos.add(rollo0);
        DetalleRollo rollo1 = DetalleRollo.builder()
                .codRollo("108-2-2")
                .pesoRollo(BigDecimal.valueOf(20.44))
                .build();
        rollos.add(rollo1);
        detalle.setDetallesRollos(rollos);

        detalles.add(detalle);

        return detalles;
    }
}
