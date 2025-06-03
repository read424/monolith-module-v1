package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.CrearOrdenIngresoUseCase;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.OrdenIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.AlmacenTipoIngresoLogisticaRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.ItemArticuloLogisticaRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.MotivoIngresoLogisticaRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto.OrdenIngresoLogisticaRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenIngresoLogisticaHandlerTest {
    @Mock
    private Validator validator;

    @Mock
    private CrearOrdenIngresoUseCase crearOrdenIngresoUseCase;

    @Mock
    private OrdenIngresoLogisticaMapper ordenIngresoMapper;

    @InjectMocks
    private OrdenIngresoLogisticaHandler handler;

    private OrdenIngresoLogisticaRequestDto requestDto;
    private OrdenIngreso ordenIngreso;
    private MockServerRequest serverRequest;

    // Detalle de orden de prueba
    private DetalleOrdenIngreso crearDetalleOrdenIngreso() {
        return DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(289)
                        .build()
                )
                .idUnidad(1)
                .lote("001120-1")
                .cantidad(BigDecimal.valueOf(240.0000))
                .idTipoProducto(1)
                .costo(BigDecimal.valueOf(2.15))
                .idMoneda(2)
                .build();
    }

    @BeforeEach
    void setup() {
        // Crear DTO y objeto de dominio para los tests
        requestDto = OrdenIngresoLogisticaRequestDto.builder()
                .id_tipo_almacen(new AlmacenTipoIngresoLogisticaRequestDto())
                .motivo(new MotivoIngresoLogisticaRequestDto())
                .id_orden(1)
                .fec_ingreso(LocalDateTime.now())
                .id_compro(1)
                .nu_serie("S001")
                .nu_comprobante("001-00001")
                .fec_ref(LocalDateTime.now())
                .id_cliente(1)
                .observacion("Test observación")
                .detalles(List.of(ItemArticuloLogisticaRequestDto.builder()
                        .idArticulo(289)
                        .id_tipo_producto(1)
                        .id_tipo_producto_fa(1)
                        .nu_lote("Test")
                        .cantidad(new BigDecimal("10.0"))
                        .idUnidad(1)
                        // Agrega otros campos obligatorios aquí
                        .build()))
                .build();


        // Configurar objeto de dominio
        ordenIngreso = OrdenIngreso.builder()
                .id(1)
                .almacen(Almacen.builder().idAlmacen(1).build())
                .motivo(Motivo.builder().idMotivo(1).descMotivo("Test Motivo").build())
                .fechaIngreso(LocalDate.now())
                .detalles(List.of(
                        DetalleOrdenIngreso.builder()
                            .articulo(Articulo.builder().id(1).build())
                            .cantidad(new BigDecimal("10.0"))
                            .build()
                ))
                .build();

        // Configurar request mock
        serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));
    }

    @Test
    void nuevoIngresoLogistica_DebeRetornarRespuestaExitosa_CuandoDatosValidos() {
        // Crear un objeto Errors vacío (sin errores)
        Errors errors = new BeanPropertyBindingResult(requestDto, "requestDto");

        // Configurar el comportamiento del validator
        doAnswer(invocation -> {
            // No hacer nada aquí, lo que simula que no hay errores
            return null;
        }).when(validator).validate(any(), any(Errors.class));// Simulamos que no hay errores
        when(ordenIngresoMapper.toOrdenIngreso(requestDto)).thenReturn(ordenIngreso);
        when(crearOrdenIngresoUseCase.crearOrdenIngresoLogistica(ordenIngreso))
                .thenReturn(Mono.just(ordenIngreso));

        // Act & Assert
        StepVerifier.create(handler.nuevoIngresoLogistica(serverRequest))
                .expectNextMatches(response ->
                        response.statusCode() == HttpStatus.OK &&
                                response.headers().getContentType().equals(MediaType.APPLICATION_JSON)
                )
                .verifyComplete();
    }

    void nuevoIngresoLogistica_DebeRetornarError_CuandoDatosInvalidos() {
        // Crear un objeto Errors con errores
        Errors errors = new BeanPropertyBindingResult(requestDto, "requestDto");

        // Arrange - Configurar validator para simular error de validación
        ServerWebInputException expectedException = new ServerWebInputException("Error de validación");

        // Crear un mock request que retorne el DTO
        MockServerRequest requestWithInvalidData = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Configurar comportamiento del validator para que se genere un error
        // Para este test necesitamos una implementación más compleja que solo mockear
        // el validator.validate, así que podemos usar doAnswer o una implementación
        // específica para este test

        // Esta es una forma simplificada - en un caso real habría que configurar
        // el objeto Errors para que tenga errores
        // Configurar el validator para añadir un error
        doAnswer(invocation -> {
            Errors errorsArg = invocation.getArgument(1);
            errorsArg.rejectValue("id_cliente", "NotNull", "El cliente es obligatorio");
            return null;
        }).when(validator).validate(any(), any(Errors.class));

        // Act & Assert
        StepVerifier.create(handler.nuevoIngresoLogistica(requestWithInvalidData))
                .expectErrorMatches(throwable ->
                        throwable instanceof ServerWebInputException &&
                                throwable.getMessage().equals("Error de validación")
                )
                .verify();
    }
}