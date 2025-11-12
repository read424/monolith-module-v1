package com.walrex.module_almacen.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.ConsultarRollosDisponiblesDevolucionUseCase;
import com.walrex.module_almacen.application.ports.output.ConsultarRollosDisponiblesPort;
import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.ConsultarRollosDisponiblesResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para consultar rollos disponibles para devolución
 * Implementa el Use Case y contiene la lógica de negocio pura
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarRollosDisponiblesDevolucionService implements ConsultarRollosDisponiblesDevolucionUseCase {

    private final ConsultarRollosDisponiblesPort consultarRollosDisponiblesPort;

    @Override
    public Flux<RolloDisponibleDevolucionDTO> consultarRollosDisponibles(Integer idCliente, Integer idArticulo) {
        log.info("🔍 Consultando rollos disponibles para devolución - Cliente: {}, Artículo: {}", idCliente,
                idArticulo);

        return validarParametrosEntrada(idCliente, idArticulo)
                .thenMany(consultarRollosDisponiblesPort.buscarRollosDisponibles(idCliente, idArticulo))
                .flatMap(this::aplicarLogicaNegocio)
                .doOnNext(rollo -> log.debug("✅ Rollo disponible encontrado: {}", rollo.getCodRollo()))
                .doOnComplete(() -> log.info("✅ Consulta de rollos disponibles completada"));
    }

    /**
     * Consulta rollos disponibles y devuelve la respuesta completa
     * Procesa el Flux y construye la respuesta final
     */
    public Mono<ConsultarRollosDisponiblesResponse> consultarRollosDisponiblesResponse(Integer idCliente,
            Integer idArticulo) {
        log.info("🔍 Consultando rollos disponibles y construyendo respuesta - Cliente: {}, Artículo: {}", idCliente,
                idArticulo);

        return consultarRollosDisponibles(idCliente, idArticulo)
                .collectList()
                .map(this::construirRespuestaExitosa)
                .onErrorResume(this::construirRespuestaError);
    }

    /**
     * Construye respuesta exitosa con los rollos encontrados
     */
    private ConsultarRollosDisponiblesResponse construirRespuestaExitosa(List<RolloDisponibleDevolucionDTO> rollos) {
        log.info("✅ Construyendo respuesta exitosa con {} rollos encontrados", rollos.size());

        // Log detallado de cada rollo encontrado
        rollos.forEach(rollo -> {
            log.info("📋 Rollo en respuesta: Código={}, Almacén={}, Peso={}, Status={}, Partida={}",
                    rollo.getCodRollo(),
                    rollo.getIdIngresoAlmacen(),
                    rollo.getPesoRollo(),
                    rollo.getStatusRolloIngreso(),
                    rollo.getCodPartida());
        });

        return ConsultarRollosDisponiblesResponse.builder()
                .rollosDisponibles(rollos)
                .totalRollos(rollos.size())
                .success(true)
                .mensaje("Rollos disponibles para devolución consultados exitosamente")
                .build();
    }

    /**
     * Construye respuesta de error
     */
    private Mono<ConsultarRollosDisponiblesResponse> construirRespuestaError(Throwable error) {
        log.error("❌ Error al consultar rollos disponibles: {}", error.getMessage(), error);

        ConsultarRollosDisponiblesResponse response = ConsultarRollosDisponiblesResponse.builder()
                .rollosDisponibles(List.of())
                .totalRollos(0)
                .success(false)
                .mensaje("Error al consultar rollos disponibles: " + error.getMessage())
                .build();

        return Mono.just(response);
    }

    private Flux<Void> validarParametrosEntrada(Integer idCliente, Integer idArticulo) {
        return Flux.defer(() -> {
            if (idCliente == null || idCliente <= 0) {
                return Flux.error(new IllegalArgumentException("El ID del cliente debe ser mayor a 0"));
            }
            if (idArticulo == null || idArticulo <= 0) {
                return Flux.error(new IllegalArgumentException("El ID del artículo debe ser mayor a 0"));
            }
            return Flux.empty();
        });
    }

    private Flux<RolloDisponibleDevolucionDTO> aplicarLogicaNegocio(RolloDisponibleDevolucionDTO rollo) {
        // Lógica de negocio: verificar que el rollo esté disponible para devolución
        if (estaDisponibleParaDevolucion(rollo)) {
            return Flux.just(rollo);
        }
        return Flux.empty();
    }

    private boolean estaDisponibleParaDevolucion(RolloDisponibleDevolucionDTO rollo) {
        // Reglas de negocio para determinar si un rollo está disponible para devolución

        // 1. Debe tener status de ingreso válido (0 o 1)
        boolean statusIngresoValido = rollo.getStatusRolloIngreso() != null &&
                (rollo.getStatusRolloIngreso().equals(0) || rollo.getStatusRolloIngreso().equals(1));

        // 2. Debe tener status de almacén activo (1)
        boolean statusAlmacenValido = rollo.getStatusRolloAlmacen() != null &&
                rollo.getStatusRolloAlmacen().equals(1);

        // 3. Si tiene partida, debe tener status válido (1 o 4, pero NO 2)
        boolean statusPartidaValido = rollo.getStatusRollPartida() == null || // Sin partida es válido
                (rollo.getStatusRollPartida().equals(1) || rollo.getStatusRollPartida().equals(4));

        log.debug("🔍 Evaluando rollo {}: statusIngreso={}, statusAlmacen={}, statusPartida={}",
                rollo.getCodRollo(), rollo.getStatusRolloIngreso(),
                rollo.getStatusRolloAlmacen(), rollo.getStatusRollPartida());

        return statusIngresoValido && statusAlmacenValido && statusPartidaValido;
    }
}