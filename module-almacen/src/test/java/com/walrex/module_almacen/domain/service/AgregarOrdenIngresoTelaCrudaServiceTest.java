package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgregarOrdenIngresoTelaCrudaServiceTest {

    @Mock
    private OrdenIngresoAdapterFactory adapterFactory;

    @Mock
    private OrdenIngresoLogisticaPort telaCrudaAdapter;

    private AgregarOrdenIngresoTelaCrudaService service;

    private OrdenIngreso ordenIngreso;

    @BeforeEach
    void setUp() {
        // Crear una implementación concreta de la clase abstracta para pruebas
        service = new AgregarOrdenIngresoTelaCrudaService(adapterFactory) {
            // No necesitamos implementar nada adicional ya que la clase base
            // ya tiene implementado el método que vamos a probar
        };

        // Preparar datos de prueba
        ordenIngreso = OrdenIngreso.builder()
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
                .build();

        List<DetalleRollo> rollos = new ArrayList<>();
        DetalleRollo rollo = DetalleRollo.builder()
                .codRollo("108-2-1")
                .pesoRollo(BigDecimal.valueOf(20.58))
                .build();
        rollos.add(rollo);

        detalle.setDetallesRollos(rollos);
        detalles.add(detalle);
        ordenIngreso.setDetalles(detalles);
    }

    @Test
    void deberiaUsarSiempreAdaptadorTelaCruda() {
        // Given
        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.just(telaCrudaAdapter));
        when(telaCrudaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngreso));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngreso);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngreso)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.TELA_CRUDA);
        verify(telaCrudaAdapter).guardarOrdenIngresoLogistica(ordenIngreso);
    }

    @Test
    void deberiaUsarAdaptadorTelaCrudaInclusivoSinRollos() {
        // Given
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
                .detallesRollos(Collections.emptyList())
                .build();
        detalles.add(detalle);
        ordenIngreso.setDetalles(detalles);

        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.just(telaCrudaAdapter));
        when(telaCrudaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngreso));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngreso);

        // Then
        StepVerifier.create(resultado)
                .expectNext(ordenIngreso)
                .verifyComplete();

        verify(adapterFactory).getAdapter(TipoOrdenIngreso.TELA_CRUDA);
        verify(telaCrudaAdapter).guardarOrdenIngresoLogistica(ordenIngreso);
    }

    @Test
    void deberiaManejarErrorDeAdaptador() {
        // Given
        RuntimeException expectedException = new RuntimeException("Error en el adaptador de tela cruda");

        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.just(telaCrudaAdapter));
        when(telaCrudaAdapter.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.error(expectedException));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngreso);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Error en el adaptador de tela cruda".equals(error.getMessage()))
                .verify();
    }

    @Test
    void deberiaManejarErrorDeFactory() {
        // Given
        RuntimeException expectedException = new RuntimeException("Error al obtener el adaptador");

        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.error(expectedException));

        // When
        Mono<OrdenIngreso> resultado = service.crearOrdenIngresoLogistica(ordenIngreso);

        // Then
        StepVerifier.create(resultado)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Error al obtener el adaptador".equals(error.getMessage()))
                .verify();
    }

    @Test
    void deberiaUsarAdaptadorTelaCrudaConOrdenIngresoNula() {
        // Given
        OrdenIngreso ordenNula = null;

        // Configurar el mock para que devuelva un Mono válido
        when(adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA))
                .thenReturn(Mono.just(telaCrudaAdapter));

        // Configurar el adaptador para que maneje la orden nula
        when(telaCrudaAdapter.guardarOrdenIngresoLogistica(null))
                .thenReturn(Mono.error(new NullPointerException("Orden no puede ser nula")));

        // When & Then
        StepVerifier.create(service.crearOrdenIngresoLogistica(ordenNula))
                .expectError(NullPointerException.class)
                .verify();

        // Verificar que sí se llamó al adaptador pero con null
        verify(adapterFactory).getAdapter(TipoOrdenIngreso.TELA_CRUDA);
        verify(telaCrudaAdapter).guardarOrdenIngresoLogistica(null);
    }
}
