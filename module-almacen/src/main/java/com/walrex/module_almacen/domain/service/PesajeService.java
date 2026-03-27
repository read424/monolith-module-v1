package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.DeleteGuideRollUseCase;
import com.walrex.module_almacen.application.ports.input.ObtenerSessionArticuloPesajeUseCase;
import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.application.ports.output.SessionArticuloPesajeOutputPort;
import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.SessionPesajeActiva;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse;
import com.walrex.module_almacen.domain.model.exceptions.ArticuloCompletadoException;
import com.walrex.module_almacen.domain.model.exceptions.RolloAsignadoPartidaException;
import com.walrex.module_almacen.domain.model.exceptions.RolloPesajeNotFoundException;
import com.walrex.module_almacen.domain.model.exceptions.SessionPesajeInvalidaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PesajeService implements PesajeUseCase, ObtenerSessionArticuloPesajeUseCase, DeleteGuideRollUseCase {

    private final PesajeOutputPort pesajeRepository;
    private final PesajeNotificationPort notificationPort;
    private final SessionArticuloPesajeOutputPort sessionOutputPort;

    @Override
    @Transactional
    public Mono<PesajeDetalle> registrarPesaje(PesajeRequest request) {
        log.info("Iniciando registro de pesaje: {} kg", request.getPeso());

        return pesajeRepository.findActiveSessionWithDetail()
                .flatMap(detalle -> {
                    log.info("Sesión activa obtenida: {}", detalle);
                    assignCodRollo(detalle);
                    detalle.setPeso_rollo(request.getPeso());
                    return pesajeRepository.saveWeight(detalle, detalle.getId_detordeningreso())
                            .flatMap(savedDetalle -> pesajeRepository.incrementPesoAlmacen(
                                            savedDetalle.getId_detordeningreso(),
                                            savedDetalle.getPeso_rollo())
                                    .thenReturn(savedDetalle))
                            .flatMap(savedDetalle ->
                                pesajeRepository.updateSessionState(savedDetalle.getId_session_hidden())
                                    .map(newStatus -> {
                                        savedDetalle.setCompletado("0".equals(newStatus));
                                        savedDetalle.setCnt_registrados(savedDetalle.getCnt_registrados() + 1);
                                        return savedDetalle;
                                    })
                            );
                })
                .flatMap(savedDetalle -> notificationPort.notifyWeightRegistered(savedDetalle)
                        .thenReturn(savedDetalle))
                .doOnSuccess(detalle -> log.info("Pesaje registrado y notificado: {}", detalle.getCod_rollo()))
                .doOnError(error -> log.error("Error al registrar pesaje: {}", error.getMessage()));
    }

    @Override
    public Mono<SessionArticuloPesajeResponse> obtenerSession(Integer idDetOrdenIngreso) {
        log.info("Consultando sesión de pesaje para id_detordeningreso: {}", idDetOrdenIngreso);

        return pesajeRepository.findActiveSessionWithDetail()
                .flatMap(activeSession -> {
                    if (!idDetOrdenIngreso.equals(activeSession.getId_detordeningreso())) {
                        log.info("Detectada sesión activa para un id_detordeningreso distinto: {}. Desactivando sesión: {}",
                                activeSession.getId_detordeningreso(), activeSession.getId_session_hidden());
                        return sessionOutputPort.updateSessionStatusToCompleted(activeSession.getId_session_hidden());
                    }
                    return Mono.empty();
                })
                .then(sessionOutputPort.findStatusByIdDetOrdenIngreso(idDetOrdenIngreso)
                        .flatMap(session -> resolveExistingSession(idDetOrdenIngreso, session))
                        .switchIfEmpty(Mono.defer(() -> executeSessionFlow(idDetOrdenIngreso))));
    }

    @Override
    @Transactional
    public Mono<Void> deleteGuideRoll(Integer idDetordenIngresoRollo) {
        log.info("Eliminando rollo de guía con id_detordeningresopeso={}", idDetordenIngresoRollo);

        return pesajeRepository.existsRolloById(idDetordenIngresoRollo)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RolloPesajeNotFoundException(
                                "No se encontró el rollo con id_detordeningresopeso=" + idDetordenIngresoRollo));
                    }

                    return pesajeRepository.findAssignedPartidaCode(idDetordenIngresoRollo)
                            .flatMap(codPartida -> Mono.<Void>error(new RolloAsignadoPartidaException(
                                    "El rollo está asignado a la partida " + codPartida)))
                            .switchIfEmpty(Mono.defer(() -> pesajeRepository.deleteRolloById(idDetordenIngresoRollo)));
                });
    }

    private Mono<SessionArticuloPesajeResponse> resolveExistingSession(
            Integer idDetOrdenIngreso, SessionPesajeActiva session) {
        if (!"0".equals(session.getStatus())) {
            return executeSessionFlow(idDetOrdenIngreso);
        }

        if (hasPendingRollsToRegister(session)) {
            log.info("Reactivando sesión cerrada id={} para id_detordeningreso={} porque faltan rollos por registrar ({}/{})",
                    session.getId(), idDetOrdenIngreso, session.getCntRegistro(), session.getCntRollos());
            return sessionOutputPort.updateSessionStatusToActive(session.getId())
                    .then(executeSessionFlow(idDetOrdenIngreso));
        }

        log.info("Artículo id_detordeningreso={} ya completó su información", idDetOrdenIngreso);
        return Mono.error(
                new ArticuloCompletadoException("El artículo ya completó su información de pesaje"));
    }

    private boolean hasPendingRollsToRegister(SessionPesajeActiva session) {
        int cntRollos = session.getCntRollos() != null ? session.getCntRollos() : 0;
        int cntRegistro = session.getCntRegistro() != null ? session.getCntRegistro() : 0;
        return cntRollos > cntRegistro;
    }

    private Mono<SessionArticuloPesajeResponse> executeSessionFlow(Integer idDetOrdenIngreso) {
        return sessionOutputPort.getArticuloWithSessionDetail(idDetOrdenIngreso)
                .switchIfEmpty(Mono.error(new SessionPesajeInvalidaException(
                        "No se encontró información para id_detordeningreso=" + idDetOrdenIngreso)))
                .flatMap(data -> validateAndProcess(data, idDetOrdenIngreso))
                .flatMap(response -> sessionOutputPort.findRollosByIdDetOrdenIngreso(idDetOrdenIngreso)
                        .collectList()
                        .map(rollos -> {
                            response.setDetails(rollos);
                            return response;
                        }));
    }

    private Mono<SessionArticuloPesajeResponse> validateAndProcess(
            ArticuloPesajeSession data, Integer idDetOrdenIngreso) {

        log.info("Datos de sesión: {}", data);
        if (data.getId() == null) {
            log.info("No existe sesión activa para id_detordeningreso={}, creando nueva", idDetOrdenIngreso);
            return sessionOutputPort
                    .insertSession(idDetOrdenIngreso, data.getNuRollos(), data.getTotalSaved().doubleValue())
                    .thenReturn(buildResponse(idDetOrdenIngreso, data));
        }

        int cntRollSaved = data.getCntRollSaved();
        int nuRollos = data.getNuRollos();

        if (cntRollSaved >= nuRollos && "1".equals(data.getStatus())) {
            log.info("Artículo id_detordeningreso={} completó todos sus rollos ({}/{}), marcando como completado",
                    idDetOrdenIngreso, cntRollSaved, nuRollos);
            return sessionOutputPort.updateSessionStatusToCompleted(data.getId())
                    .then(Mono.error(new ArticuloCompletadoException(
                            "El artículo ha registrado todos sus rollos y fue marcado como completado")));
        }

        return Mono.just(buildResponse(idDetOrdenIngreso, data));
    }

    private SessionArticuloPesajeResponse buildResponse(Integer idDetOrdenIngreso, ArticuloPesajeSession data) {
        return SessionArticuloPesajeResponse.builder()
                .id_detordeningreso(idDetOrdenIngreso)
                .cantidad(data.getCntRollSaved())
                .total_kg(data.getTotalSaved().doubleValue())
                .build();
    }

    private void assignCodRollo(PesajeDetalle detalle) {
        if (detalle.getLote() == null) {
            throw new SessionPesajeInvalidaException(
                    "El lote del artículo no está definido en la sesión activa (id_detordeningreso="
                    + detalle.getId_detordeningreso() + ")");
        }
        int seq = detalle.getCnt_registrados() != null ? detalle.getCnt_registrados() + 1 : 1;
        String codRollo = detalle.getLote() + "-" + seq;
        log.debug("Asignando cod_rollo: {}", codRollo);
        detalle.setCod_rollo(codRollo);
    }
}
