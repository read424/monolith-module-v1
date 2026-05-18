package com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.user.module_users.application.ports.input.ListPersonalUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalHandler {

    private final ListPersonalUseCase listPersonalUseCase;

    public Mono<ServerResponse> listPersonal(ServerRequest request) {
        try {
            String search = request.queryParam("search").orElse(null);
            int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
            int size = request.queryParam("size").map(Integer::parseInt).orElse(10);

            List<Integer> idAreas = request.queryParams().getOrDefault("id_area[]", List.of())
                    .stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            return listPersonalUseCase.listPersonal(search, idAreas, page, size)
                    .flatMap(response -> ServerResponse.ok().bodyValue(response))
                    .onErrorResume(error -> {
                        log.error("Error listando personal: {}", error.getMessage(), error);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });
        } catch (NumberFormatException ex) {
            return ServerResponse.badRequest().bodyValue("Los parámetros page, size e id_area deben ser numéricos");
        }
    }
}
