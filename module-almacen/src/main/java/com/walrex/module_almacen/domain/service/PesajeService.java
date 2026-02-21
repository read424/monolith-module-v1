package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.ObtenerSessionArticuloPesajeUseCase;
import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.application.ports.output.PesajeOutputPort;
import com.walrex.module_almacen.application.ports.output.SessionArticuloPesajeOutputPort;
import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import com.walrex.module_almacen.domain.model.dto.SessionArticuloPesajeResponse;
import com.walrex.module_almacen.domain.model.exceptions.ArticuloCompletadoException;
import com.walrex.module_almacen.domain.model.exceptions.SessionPesajeInvalidaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class PesajeService implements PesajeUseCase, ObtenerSessionArticuloPesajeUseCase {

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

        return sessionOutputPort.findStatusByIdDetOrdenIngreso(idDetOrdenIngreso)
                .flatMap(status -> {
                    if ("0".equals(status)) {
                        log.info("Artículo id_detordeningreso={} ya completó su información", idDetOrdenIngreso);
                        return Mono.<SessionArticuloPesajeResponse>error(
                                new ArticuloCompletadoException("El artículo ya completó su información de pesaje"));
                    }
                    return executeSessionFlow(idDetOrdenIngreso);
                })
                .switchIfEmpty(Mono.defer(() -> executeSessionFlow(idDetOrdenIngreso)));
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

    /**
     * Construye el código de rollo definitivo concatenando el lote base con el
     * correlativo actual de la sesión: {lote}-{cnt_registrados + 1}
     * Ejemplo: "LT001-ARTXX" + "-" + 2  →  "LT001-ARTXX-2"
     */
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
