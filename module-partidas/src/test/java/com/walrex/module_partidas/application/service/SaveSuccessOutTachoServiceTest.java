package com.walrex.module_partidas.application.service;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_partidas.application.ports.output.SaveSuccessOutTachoPort;
import com.walrex.module_partidas.domain.model.*;
import com.walrex.module_partidas.domain.model.dto.SaveSuccessOutTachoRequest;
import com.walrex.module_partidas.domain.service.SaveSuccessOutTachoService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Test unitario para SaveSuccessOutTachoService
 * Sigue TDD y utiliza StepVerifier para testing reactivo
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)
class SaveSuccessOutTachoServiceTest {

        @Mock
        private SaveSuccessOutTachoPort saveSuccessOutTachoPort;

        @InjectMocks
        private SaveSuccessOutTachoService service;

        private SaveSuccessOutTachoRequest request;
        private SuccessPartidaTacho request_nuevo;
        private List<ItemRollo> rollosDisponibles;
        private List<ProcesoPartida> procesos;

        @BeforeEach
        void setUp() {
                // Crear request válido
                SaveSuccessOutTachoRequest.DetalleRollo detalle = SaveSuccessOutTachoRequest.DetalleRollo.builder()
                                .codRollo("1787-1-1")
                                .pesoRollo("21.48")
                                .pesoAcabado(BigDecimal.ZERO)
                                .idIngresopeso("3134524")
                                .idDetPartida("518038")
                                .idIngresoAlmacen("307764")
                                .idRolloIngreso("3135766")
                                .despachado(false)
                                .idAlmacen("5")
                                .idOrdeningreso("307651")
                                .selected(true)
                                .delete(0)
                                .build();

                request = SaveSuccessOutTachoRequest.builder()
                                .idPartida(55702)
                                .idAlmacen(36)
                                .idCliente(45544)
                                .rollos(Arrays.asList(detalle))
                                .build();

                request_nuevo = SuccessPartidaTacho.builder()
                                .idPartida(55702)
                                .idAlmacen(36)
                                .idCliente(45544)
                                .build();

                // Crear rollos disponibles
                ItemRollo rolloDisponible = ItemRollo.builder()
                                .codRollo("1787-1-1")
                                .despacho(Boolean.FALSE)
                                .idAlmacen(5)
                                .idDetPartida(518038)
                                .idIngresoAlmacen(307764)
                                .idIngresopeso(3134524)
                                .idOrdeningreso(307651)
                                .idRolloIngreso(3135766)
                                .isParentRollo(1)
                                .noAlmacen("Almacén Principal")
                                .numChildRoll(0)
                                .pesoAcabado(0.0)
                                .pesoRollo(21.48)
                                .pesoSaldo(21.48)
                                .pesoSalida(21.48)
                                .status(1)
                                .build();

                rollosDisponibles = Arrays.asList(rolloDisponible);

                // Crear procesos
                ProcesoPartida proceso = ProcesoPartida.builder()
                                .idCliente(45544)
                                .idPartida(55702)
                                .idPartidaMaquina(123)
                                .idRuta(456)
                                .idArticulo(789)
                                .idProceso(101)
                                .idDetRuta(202)
                                .noProceso("PROCESO_001")
                                .idAlmacen(37) // Próximo almacén
                                .idMaquina(303)
                                .idTipoMaquina(404)
                                .iniciado(false)
                                .finalizado(false)
                                .isPendiente(true)
                                .status(0)
                                .isMainProceso(true)
                                .descMaq("Máquina Proceso")
                                .build();

                procesos = Arrays.asList(proceso);
        }

        @Test
        @DisplayName("Debería procesar exitosamente la salida de tacho")
        void deberiaProcesarExitosamenteSalidaTacho() {
                // Given
                when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                                .thenReturn(Mono.just(rollosDisponibles));
                when(saveSuccessOutTachoPort.consultarProcesosPartida(55702))
                                .thenReturn(Mono.just(procesos));
                when(saveSuccessOutTachoPort.crearOrdenIngreso(45544, 5,37))
                                .thenReturn(Mono.just(1001));
                when(saveSuccessOutTachoPort.crearDetalleOrdenIngreso(1001, 1, 1, BigDecimal.ZERO, "1", Integer.valueOf(1),
                                55702))
                                .thenReturn(Mono.just(2001));
                when(saveSuccessOutTachoPort.crearDetallePesoOrdenIngreso(1001, "1787-1-1", new BigDecimal("21.48"),
                                2001,
                                3135766))
                                .thenReturn(Mono.just(3001));
                when(saveSuccessOutTachoPort.actualizarStatusDetallePeso(3134524))
                                .thenReturn(Mono.empty());

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .verifyComplete();

                verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
                verify(saveSuccessOutTachoPort).consultarProcesosPartida(55702);
                verify(saveSuccessOutTachoPort).crearOrdenIngreso(45544, 5,37);
                verify(saveSuccessOutTachoPort).crearDetalleOrdenIngreso(1001, 1, 1, BigDecimal.ZERO, "1", Integer.valueOf(1),
                                55702);
                verify(saveSuccessOutTachoPort).crearDetallePesoOrdenIngreso(1001, "1787-1-1", new BigDecimal("21.48"),
                                2001,
                                3135766);
                verify(saveSuccessOutTachoPort).actualizarStatusDetallePeso(3134524);
        }

        @Test
        @DisplayName("Debería retornar error cuando ID de partida es null")
        void deberiaRetornarErrorCuandoIdPartidaEsNull() {
                // Given
                request.setIdPartida(null);

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verifyNoInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando ID de almacén es null")
        void deberiaRetornarErrorCuandoIdAlmacenEsNull() {
                // Given
                request.setIdAlmacen(null);

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verifyNoInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando lista de detalles está vacía")
        void deberiaRetornarErrorCuandoListaDetallesEstaVacia() {
                // Given
                //request.setDetalles(Collections.emptyList());

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verifyNoInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando no hay rollos seleccionados")
        void deberiaRetornarErrorCuandoNoHayRollosSeleccionados() {
                // Given
                //request.getDetalles().get(0).setSelected(false);

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verifyNoInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando no hay rollos disponibles")
        void deberiaRetornarErrorCuandoNoHayRollosDisponibles() {
                // Given
                when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                                .thenReturn(Mono.just(Collections.emptyList()));

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
                verifyNoMoreInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando rollo seleccionado no está disponible")
        void deberiaRetornarErrorCuandoRolloSeleccionadoNoEstaDisponible() {
                // Given
                when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                                .thenReturn(Mono.just(rollosDisponibles));

                // Cambiar código de rollo para que no coincida
                //request.getDetalles().get(0).setCodRollo("ROLLO-INEXISTENTE");

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
                verifyNoMoreInteractions(saveSuccessOutTachoPort);
        }

        @Test
        @DisplayName("Debería retornar error cuando no hay procesos pendientes")
        void deberiaRetornarErrorCuandoNoHayProcesosPendientes() {
                // Given
                when(saveSuccessOutTachoPort.consultarRollosDisponibles(55702, 36))
                                .thenReturn(Mono.just(rollosDisponibles));

                // Crear proceso no pendiente
                ProcesoPartida procesoNoPendiente = ProcesoPartida.builder()
                                .idPartida(55702)
                                .isPendiente(false)
                                .build();

                when(saveSuccessOutTachoPort.consultarProcesosPartida(55702))
                                .thenReturn(Mono.just(Arrays.asList(procesoNoPendiente)));

                // When & Then
                StepVerifier.create(service.saveSuccessOutTacho(request_nuevo))
                                .expectError(IllegalArgumentException.class)
                                .verify();

                verify(saveSuccessOutTachoPort).consultarRollosDisponibles(55702, 36);
                verify(saveSuccessOutTachoPort).consultarProcesosPartida(55702);
                verifyNoMoreInteractions(saveSuccessOutTachoPort);
        }
}
