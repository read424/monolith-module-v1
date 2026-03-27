package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.output.UpdateGuideArticleOutputPort;
import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleConflictException;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateGuideArticleServiceTest {

    @Mock
    private UpdateGuideArticleOutputPort outputPort;

    private UpdateGuideArticleService service;

    @BeforeEach
    void setUp() {
        service = new UpdateGuideArticleService(outputPort);
    }

    @Test
    void updateGuideArticle_ReturnsNotFound_WhenDetailDoesNotExist() {
        UpdateGuideArticleRequest request = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(20.0)
                .nu_rollos(2)
                .build();

        when(outputPort.findIdOrdenIngresoByIdDetalleOrden(10)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateGuideArticle(10, request))
                .expectErrorMatches(error -> error instanceof GuideArticleNotFoundException)
                .verify();
    }

    @Test
    void updateGuideArticle_ReturnsConflict_WhenRequestedRollsAreLessThanExisting() {
        UpdateGuideArticleRequest request = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(20.0)
                .nu_rollos(1)
                .build();

        when(outputPort.findIdOrdenIngresoByIdDetalleOrden(10)).thenReturn(Mono.just(100L));
        when(outputPort.countExistingRolls(10)).thenReturn(Mono.just(2L));

        StepVerifier.create(service.updateGuideArticle(10, request))
                .expectErrorMatches(error -> error instanceof GuideArticleConflictException
                        && error.getMessage().contains("menor"))
                .verify();

        verify(outputPort, never()).existsProductionOrderByIdOrdenIngreso(anyLong());
    }

    @Test
    void updateGuideArticle_ReturnsConflict_WhenProductionOrderExists() {
        UpdateGuideArticleRequest request = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(20.0)
                .nu_rollos(3)
                .build();

        when(outputPort.findIdOrdenIngresoByIdDetalleOrden(10)).thenReturn(Mono.just(100L));
        when(outputPort.countExistingRolls(10)).thenReturn(Mono.just(2L));
        when(outputPort.existsProductionOrderByIdOrdenIngreso(100L)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateGuideArticle(10, request))
                .expectErrorMatches(error -> error instanceof GuideArticleConflictException
                        && error.getMessage().contains("orden de producción"))
                .verify();

        verify(outputPort, never()).existsAssignedPartidaByIdDetalleOrden(anyInt());
    }

    @Test
    void updateGuideArticle_ReturnsConflict_WhenAnyRollIsAssignedToPartida() {
        UpdateGuideArticleRequest request = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(20.0)
                .nu_rollos(3)
                .build();

        when(outputPort.findIdOrdenIngresoByIdDetalleOrden(10)).thenReturn(Mono.just(100L));
        when(outputPort.countExistingRolls(10)).thenReturn(Mono.just(2L));
        when(outputPort.existsProductionOrderByIdOrdenIngreso(100L)).thenReturn(Mono.just(false));
        when(outputPort.existsAssignedPartidaByIdDetalleOrden(10)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateGuideArticle(10, request))
                .expectErrorMatches(error -> error instanceof GuideArticleConflictException
                        && error.getMessage().contains("partida"))
                .verify();

        verify(outputPort, never()).updateGuideArticle(anyInt(), any());
    }

    @Test
    void updateGuideArticle_Completes_WhenValidationsPass() {
        UpdateGuideArticleRequest request = UpdateGuideArticleRequest.builder()
                .id_articulo(1)
                .peso_ref(20.0)
                .nu_rollos(3)
                .build();

        when(outputPort.findIdOrdenIngresoByIdDetalleOrden(10)).thenReturn(Mono.just(100L));
        when(outputPort.countExistingRolls(10)).thenReturn(Mono.just(2L));
        when(outputPort.existsProductionOrderByIdOrdenIngreso(100L)).thenReturn(Mono.just(false));
        when(outputPort.existsAssignedPartidaByIdDetalleOrden(10)).thenReturn(Mono.just(false));
        when(outputPort.updateGuideArticle(10, request)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateGuideArticle(10, request))
                .verifyComplete();

        verify(outputPort).updateGuideArticle(10, request);
    }
}
