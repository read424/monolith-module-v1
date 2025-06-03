package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class OrdenIngresoAdapterFactoryImplTest {
    @Mock
    private OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter;

    @Mock
    private OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter;

    @Mock
    private OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter;

    private OrdenIngresoAdapterFactoryImpl factory;

    @BeforeEach
    void setUp() {
        factory = new OrdenIngresoAdapterFactoryImpl(ordenIngresoLogisticaAdapter, ordenIngresoTelaCrudaAdapter, ordenIngresoTransformacionAdapter);
    }

    @Test
    void shouldReturnDefaultAdapterWhenTipoOrdenIsNull() {
        // When
        Mono<OrdenIngresoLogisticaPort> result = factory.getAdapter(null);

        // Then
        StepVerifier.create(result)
                .expectNext(ordenIngresoLogisticaAdapter)
                .verifyComplete();
    }

    @Test
    void shouldReturnTelaCrudaAdapterWhenTipoOrdenIsTelaCruda() {
        // When
        Mono<OrdenIngresoLogisticaPort> result = factory.getAdapter(TipoOrdenIngreso.TELA_CRUDA);

        // Then
        StepVerifier.create(result)
                .expectNext(ordenIngresoTelaCrudaAdapter)
                .verifyComplete();
    }

    @Test
    void shouldReturnDefaultAdapterWhenTipoOrdenIsLogisticaGeneral() {
        // When
        Mono<OrdenIngresoLogisticaPort> result = factory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL);

        // Then
        StepVerifier.create(result)
                .expectNext(ordenIngresoLogisticaAdapter)
                .verifyComplete();
    }

    @Test
    void shouldReturnDefaultAdapterForAnyOtherTipoOrden() {
        // Assuming there are other enum values not explicitly handled in the switch
        // For example, if we have another value in the future
        // When
        Mono<OrdenIngresoLogisticaPort> result = factory.getAdapter(TipoOrdenIngreso.OTRO_TIPO); // Hipotético

        // Then
        StepVerifier.create(result)
                .expectNext(ordenIngresoLogisticaAdapter)
                .verifyComplete();
    }

}
