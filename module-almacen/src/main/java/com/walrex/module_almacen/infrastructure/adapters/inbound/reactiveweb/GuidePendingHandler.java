package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.GuidePendingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuidePendingHandler {

    private final GuidePendingUseCase guidePendingUseCase;

    public Mono<ServerResponse> getPendingGuides(ServerRequest request) {
        String fecRegistroStr = request.queryParam("fec_registro").orElse(LocalDate.now().toString());

        try {
            LocalDate date = LocalDate.parse(fecRegistroStr);
            log.info("GuidePendingHandler request fec_registro={}", date);
            return guidePendingUseCase.getPendingGuides(date)
                    .collectList()
                    .doOnNext(list -> log.info(
                            "GuidePendingHandler response guides={} peso_ref values={}",
                            list.size(),
                            list.stream()
                                    .flatMap(response -> response.getDetails().stream())
                                    .map(detail -> detail.getId_detordeningreso() + ":" + detail.getPeso_ref())
                                    .toList()))
                    .flatMap(list -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(list))
                    .onErrorResume(e -> {
                        log.error("Error al obtener guías pendientes: {}", e.getMessage(), e);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(e.getMessage());
                    });
        } catch (DateTimeParseException e) {
            log.error("Formato de fecha inválido: {}", fecRegistroStr);
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("Formato de fecha inválido. Use YYYY-MM-DD");
        }
    }
}
