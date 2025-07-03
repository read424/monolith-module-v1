package com.walrex.module_almacen.domain.service;

import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.walrex.module_almacen.application.ports.output.ConsultarRollosDisponiblesPort;
import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Test unitario para ConsultarRollosDisponiblesDevolucionService
 * Usa StepVerifier para testing reactivo siguiendo las reglas establecidas
 */
@ExtendWith(MockitoExtension.class)
class ConsultarRollosDisponiblesDevolucionServiceTest {

    @Mock
    private ConsultarRollosDisponiblesPort consultarRollosDisponiblesPort;

    private ConsultarRollosDisponiblesDevolucionService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarRollosDisponiblesDevolucionService(consultarRollosDisponiblesPort);
    }

    @Test
    void consultarRollosDisponibles_DeberiaRetornarRollosDisponibles_CuandoParametrosSonValidos() {
        // Arrange
        Integer idCliente = 1;
        Integer idArticulo = 100;

        RolloDisponibleDevolucionDTO rolloMock = RolloDisponibleDevolucionDTO.builder()
                .idDetordeningresopeso(1)
                .codRollo("ROLLO-001")
                .idOrdeningreso(1)
                .codigoOrdenIngreso("ORD-001")
                .fechaIngreso(LocalDate.now())
                .idArticulo(idArticulo)
                .statusRolloIngreso(1) // ACTIVO
                .statusRolloAlmacen(1) // ACTIVO
                .statusRollPartida(1) // ACTIVO
                .build();

        when(consultarRollosDisponiblesPort.buscarRollosDisponibles(idCliente, idArticulo))
                .thenReturn(Flux.just(rolloMock));

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .expectNext(rolloMock)
                .verifyComplete();
    }

    @Test
    void consultarRollosDisponibles_DeberiaRetornarError_CuandoIdClienteEsNulo() {
        // Arrange
        Integer idCliente = null;
        Integer idArticulo = 100;

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("El ID del cliente debe ser mayor a 0"))
                .verify();
    }

    @Test
    void consultarRollosDisponibles_DeberiaRetornarError_CuandoIdArticuloEsNulo() {
        // Arrange
        Integer idCliente = 1;
        Integer idArticulo = null;

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("El ID del artículo debe ser mayor a 0"))
                .verify();
    }

    @Test
    void consultarRollosDisponibles_DeberiaRetornarError_CuandoIdClienteEsCero() {
        // Arrange
        Integer idCliente = 0;
        Integer idArticulo = 100;

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("El ID del cliente debe ser mayor a 0"))
                .verify();
    }

    @Test
    void consultarRollosDisponibles_DeberiaFiltrarRollosConPartidaPorDespachar_CuandoStatusPartidaEsDos() {
        // Arrange
        Integer idCliente = 1;
        Integer idArticulo = 100;

        RolloDisponibleDevolucionDTO rolloInactivo = RolloDisponibleDevolucionDTO.builder()
                .idDetordeningresopeso(1)
                .codRollo("ROLLO-002")
                .statusRolloIngreso(1) // ACTIVO
                .statusRolloAlmacen(1) // ACTIVO
                .statusRollPartida(2) // POR DESPACHAR - NO DISPONIBLE
                .build();

        when(consultarRollosDisponiblesPort.buscarRollosDisponibles(idCliente, idArticulo))
                .thenReturn(Flux.just(rolloInactivo));

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .verifyComplete(); // No debería devolver elementos
    }

    @Test
    void consultarRollosDisponibles_DeberiaFiltrarRollosConStatusInvalido_CuandoStatusEsDos() {
        // Arrange
        Integer idCliente = 1;
        Integer idArticulo = 100;

        RolloDisponibleDevolucionDTO rolloInvalidoStatus = RolloDisponibleDevolucionDTO.builder()
                .idDetordeningresopeso(1)
                .codRollo("ROLLO-003")
                .statusRolloIngreso(2) // STATUS INVÁLIDO (por despachar)
                .statusRolloAlmacen(1) // ACTIVO
                .statusRollPartida(1) // ACTIVO
                .build();

        when(consultarRollosDisponiblesPort.buscarRollosDisponibles(idCliente, idArticulo))
                .thenReturn(Flux.just(rolloInvalidoStatus));

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .verifyComplete(); // No debería devolver elementos
    }

    @Test
    void consultarRollosDisponibles_DeberiaRetornarRollosLiquidados_CuandoStatusPartidaEsCuatro() {
        // Arrange
        Integer idCliente = 1;
        Integer idArticulo = 100;

        RolloDisponibleDevolucionDTO rolloLiquidado = RolloDisponibleDevolucionDTO.builder()
                .idDetordeningresopeso(1)
                .codRollo("ROLLO-004")
                .statusRolloIngreso(1) // ACTIVO
                .statusRolloAlmacen(1) // ACTIVO
                .statusRollPartida(4) // LIQUIDADO - SÍ DISPONIBLE
                .build();

        when(consultarRollosDisponiblesPort.buscarRollosDisponibles(idCliente, idArticulo))
                .thenReturn(Flux.just(rolloLiquidado));

        // Act & Assert
        StepVerifier.create(service.consultarRollosDisponibles(idCliente, idArticulo))
                .expectNext(rolloLiquidado)
                .verifyComplete();
    }
}