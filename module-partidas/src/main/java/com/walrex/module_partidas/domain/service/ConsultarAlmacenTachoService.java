package com.walrex.module_partidas.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.walrex.module_partidas.application.ports.input.ConsultarAlmacenTachoUseCase;
import com.walrex.module_partidas.application.ports.output.ConsultarAlmacenTachoPort;
import com.walrex.module_partidas.domain.mapper.AlmacenTachoResponseDTOMapper;
import com.walrex.module_partidas.domain.model.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarAlmacenTachoService implements ConsultarAlmacenTachoUseCase {

    private final ConsultarAlmacenTachoPort consultarAlmacenTachoPort;
    private final AlmacenTachoResponseDTOMapper almacenTachoResponseMapper;


    @Override
    public Mono<AlmacenTachoResponseDTO> listarPartidasInTacho(ConsultarAlmacenTachoRequest request) {
        log.info("Ejecutando consulta de almacén tacho para almacén ID: {} con paginación: page={}, size={}",
                request.getIdAlmacen(), request.getPage(), request.getNumRows());

        return consultarAlmacenTachoPort.consultarAlmacenTacho(request)
                .map(almacenTachoResponseMapper::toDTO)
                .flatMap(response ->{
                    return Flux.fromIterable(response.getPartidas())
                        .map(this::enriquecerConTiempoTranscurrido)
                        .collectList()
                        .map(partidasEnriquecidas -> {
                            response.setPartidas(partidasEnriquecidas);
                            return response;
                        });
                })
                .doOnNext(resultado -> log.debug("Procesadas {} partidas para almacén ID: {}",
                        resultado.getPartidas().size(),
                        request.getIdAlmacen()
                        )
                )
                .doOnSuccess(resultado -> log.info("Consulta de almacén tacho completada exitosamente para almacén ID: {}",
                        request.getIdAlmacen())
                )
                .doOnError(error -> log.error("Error en consulta de almacén tacho para almacén ID {}: {}",
                        request.getIdAlmacen(), error.getMessage()));
    }

    /**
     * Enriquece el modelo de dominio con información adicional calculada
     *
     * @param partida Partida de almacén tacho
     * @return Partida enriquecida
     */
    private PartidaTachoResponse enriquecerConTiempoTranscurrido(PartidaTachoResponse partida) {
        if (partida.getFecRegistro() != null) {
            long segundosTranscurridos = calcularTiempoTranscurrido(partida.getFecRegistro());
            log.debug("Partida ID {} enriquecida: Registrada hace {} segundos",
                    partida.getIdPartida(), segundosTranscurridos);
                    partida.setTimeElapsed(segundosTranscurridos);
        }

        return partida;
    }

    /**
     * Calcula los segundos transcurridos desde la fecha de registro hasta la
     * fecha actual
     *
     * @param fechaRegistro Fecha de registro de la partida
     * @return Segundos transcurridos
     */
    private long calcularTiempoTranscurrido(LocalDateTime fechaRegistro) {
        if (fechaRegistro == null) {
            return 0L;
        }

        LocalDateTime fechaActual = LocalDateTime.now();
        Duration duracion = Duration.between(fechaRegistro, fechaActual);
        return duracion.toSeconds();
    }
}
