package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.GuideAdditionUseCase;
import com.walrex.module_almacen.application.ports.input.UpdateGuideArticleUseCase;
import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleConflictException;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideAdditionHandlerTest {

    @Mock
    private GuideAdditionUseCase guideAdditionUseCase;

    @Mock
    private UpdateGuideArticleUseCase updateGuideArticleUseCase;

    private GuideAdditionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GuideAdditionHandler(guideAdditionUseCase, updateGuideArticleUseCase);
    }

    @Test
    void updateGuideArticle_ReturnsOk_WhenUpdateSucceeds() {
        UpdateGuideArticleRequest requestBody = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(25.0)
                .nu_rollos(3)
                .build();

        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetalleOrden", "10")
                .body(Mono.just(requestBody));

        when(updateGuideArticleUseCase.updateGuideArticle(10, requestBody)).thenReturn(Mono.empty());

        Mono<ServerResponse> response = handler.updateGuideArticle(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void updateGuideArticle_ReturnsNotFound_WhenDetailDoesNotExist() {
        UpdateGuideArticleRequest requestBody = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(25.0)
                .nu_rollos(3)
                .build();

        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetalleOrden", "10")
                .body(Mono.just(requestBody));

        when(updateGuideArticleUseCase.updateGuideArticle(10, requestBody))
                .thenReturn(Mono.error(new GuideArticleNotFoundException("No existe")));

        Mono<ServerResponse> response = handler.updateGuideArticle(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.NOT_FOUND
                        && MediaType.APPLICATION_JSON.equals(serverResponse.headers().getContentType()))
                .verifyComplete();
    }

    @Test
    void updateGuideArticle_ReturnsConflict_WhenValidationFails() {
        UpdateGuideArticleRequest requestBody = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(25.0)
                .nu_rollos(3)
                .build();

        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("idDetalleOrden", "10")
                .body(Mono.just(requestBody));

        when(updateGuideArticleUseCase.updateGuideArticle(10, requestBody))
                .thenReturn(Mono.error(new GuideArticleConflictException("Conflicto")));

        Mono<ServerResponse> response = handler.updateGuideArticle(request);

        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.CONFLICT
                        && MediaType.APPLICATION_JSON.equals(serverResponse.headers().getContentType()))
                .verifyComplete();
    }
}
