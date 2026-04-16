package com.walrex.module_partidas.application.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.application.ports.output.OrdenSalidaPersistencePort;
import com.walrex.module_partidas.application.ports.output.SaveSuccessOutTachoPort;
import com.walrex.module_partidas.application.ports.output.WebSocketNotificationPort;
import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.ItemRolloProcess;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.domain.model.SuccessPartidaTacho;
import com.walrex.module_partidas.domain.service.SaveSuccessOutTachoService;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.OrdenIngresoCompletaProjection;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SaveSuccessOutTachoServiceTest {

    @Mock
    private SaveSuccessOutTachoPort saveSuccessOutTachoPort;

    @Mock
    private OrdenSalidaPersistencePort ordenSalidaPersistencePort;

    @Mock
    private WebSocketNotificationPort webSocketNotificationPort;

    @InjectMocks
    private SaveSuccessOutTachoService service;

    private SuccessPartidaTacho request;
    private ItemRolloProcess rolloSeleccionado;
    private List<ItemRollo> rollosDisponibles;
    private List<ProcesoPartida> procesos;

    @BeforeEach
    void setUp() {
        rolloSeleccionado = ItemRolloProcess.builder()
                .codRollo("1787-1-1")
                .pesoRollo(21.48)
                .idOrdenIngreso(307651)
                .idIngresoPeso(3134524)
                .idIngresoAlmacen(307764)
                .idRolloIngreso(3135766)
                .idDetPartida(518038)
                .idAlmacen(36)
                .selected(true)
                .status(1)
                .delete(0)
                .build();

        request = SuccessPartidaTacho.builder()
                .idPartida(55702)
                .idAlmacen(36)
                .idCliente(45544)
                .idArticulo(789)
                .lote("L-001")
                .idUnidad(1)
                .idSupervisor(99)
                .rollos(List.of(rolloSeleccionado))
                .build();

        rollosDisponibles = List.of(
                ItemRollo.builder()
                        .codRollo("1787-1-1")
                        .idIngresopeso(3134524)
                        .idIngresoAlmacen(307764)
                        .idDetPartida(518038)
                        .idRolloIngreso(3135766)
                        .pesoRollo(21.48)
                        .build(),
                ItemRollo.builder()
                        .codRollo("1787-1-2")
                        .idIngresopeso(3134525)
                        .idIngresoAlmacen(307764)
                        .idDetPartida(518039)
                        .idRolloIngreso(3135767)
                        .pesoRollo(18.00)
                        .build());

        procesos = List.of(ProcesoPartida.builder()
                .idPartida(55702)
                .idAlmacen(37)
                .noProceso("PROCESO_001")
                .isPendiente(true)
                .build());
    }

    @Test
    @DisplayName("procesa exitosamente la salida de tacho")
    void procesaExitosamenteSalidaTacho() {
        when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                .thenReturn(Mono.just(rollosDisponibles));
        when(saveSuccessOutTachoPort.consultarProcesosPartida(55702))
                .thenReturn(Mono.just(procesos));
        when(saveSuccessOutTachoPort.crearOrdenIngreso(45544, 36, 37))
                .thenReturn(Mono.just(1001));
        when(saveSuccessOutTachoPort.consultarOrdenIngresoCompleta(1001))
                .thenReturn(Mono.just(new OrdenIngresoCompletaProjection(1001, 45544, "ALGT-I46331", 37)));
        when(ordenSalidaPersistencePort.crearOrdenSalida(eq(36), eq(37), any(), eq(99), eq(55702)))
                .thenReturn(Mono.just(2001));
        when(saveSuccessOutTachoPort.crearDetalleOrdenIngreso(eq(1001), eq(789), eq(1),
                eq(BigDecimal.valueOf(21.48)), eq("L-001"), eq(1), eq(55702)))
                .thenReturn(Mono.just(3001));
        when(ordenSalidaPersistencePort.crearDetalleOrdenSalida(2001, 789, 1, 1, 55702,
                BigDecimal.valueOf(21.48), 3001))
                .thenReturn(Mono.just(4001));
        when(saveSuccessOutTachoPort.crearDetallePesoOrdenIngreso(eq(1001), eq("1787-1-1"),
                any(BigDecimal.class), eq(3001), eq(3134524)))
                .thenReturn(Mono.just(5001));
        when(ordenSalidaPersistencePort.crearDetOrdenSalidaPeso(eq(4001), eq(2001), eq("1787-1-1"),
                any(BigDecimal.class), eq(518038), eq(3134524)))
                .thenReturn(Mono.empty());
        when(saveSuccessOutTachoPort.getCantidadRollosOrdenIngreso(307764))
                .thenReturn(Mono.just(2));
        when(saveSuccessOutTachoPort.actualizarStatusDetallePeso(3135766))
                .thenReturn(Mono.empty());
        when(webSocketNotificationPort.enviarNotificacionAlmacen(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1001, response.getIdOrdeningreso());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getCntRollos());
                    org.junit.jupiter.api.Assertions.assertEquals(37, response.getIdAlmacen());
                })
                .verifyComplete();

        verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
        verify(saveSuccessOutTachoPort).consultarProcesosPartida(55702);
        verify(webSocketNotificationPort).enviarNotificacionAlmacen(any());
    }

    @Test
    @DisplayName("retorna error cuando idPartida es null")
    void retornaErrorCuandoIdPartidaEsNull() {
        request.setIdPartida(null);

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(saveSuccessOutTachoPort, ordenSalidaPersistencePort, webSocketNotificationPort);
    }

    @Test
    @DisplayName("retorna error cuando idAlmacen es null")
    void retornaErrorCuandoIdAlmacenEsNull() {
        request.setIdAlmacen(null);

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(saveSuccessOutTachoPort, ordenSalidaPersistencePort, webSocketNotificationPort);
    }

    @Test
    @DisplayName("retorna error cuando lista de rollos está vacía")
    void retornaErrorCuandoListaDeRollosEstaVacia() {
        request.setRollos(List.of());

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(saveSuccessOutTachoPort, ordenSalidaPersistencePort, webSocketNotificationPort);
    }

    @Test
    @DisplayName("retorna error cuando no hay rollos seleccionados")
    void retornaErrorCuandoNoHayRollosSeleccionados() {
        request.setRollos(List.of(ItemRolloProcess.builder()
                .codRollo("1787-1-1")
                .pesoRollo(21.48)
                .idOrdenIngreso(307651)
                .idIngresoPeso(3134524)
                .idIngresoAlmacen(307764)
                .idRolloIngreso(3135766)
                .idDetPartida(518038)
                .idAlmacen(36)
                .selected(false)
                .status(1)
                .delete(0)
                .build()));

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(saveSuccessOutTachoPort, ordenSalidaPersistencePort, webSocketNotificationPort);
    }

    @Test
    @DisplayName("retorna error cuando no hay rollos disponibles")
    void retornaErrorCuandoNoHayRollosDisponibles() {
        when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                .thenReturn(Mono.just(List.of()));

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
        verifyNoMoreInteractions(saveSuccessOutTachoPort);
    }

    @Test
    @DisplayName("retorna error cuando no hay procesos pendientes")
    void retornaErrorCuandoNoHayProcesosPendientes() {
        when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                .thenReturn(Mono.just(rollosDisponibles));
        when(saveSuccessOutTachoPort.consultarProcesosPartida(55702))
                .thenReturn(Mono.just(List.of(ProcesoPartida.builder()
                        .idPartida(55702)
                        .isPendiente(false)
                        .build())));

        StepVerifier.create(service.saveSuccessOutTacho(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
        verify(saveSuccessOutTachoPort).consultarProcesosPartida(55702);
        verifyNoMoreInteractions(saveSuccessOutTachoPort);
    }
}
