package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BaseInventarioAdapterTest {
    @Mock
    private ArticuloAlmacenRepository articuloRepository;

    private TestableBaseInventarioAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TestableBaseInventarioAdapter(articuloRepository);
    }

    @Test
    @DisplayName("Debe aplicar conversión cuando idUnidad es diferente a idUnidadConsumo")
    void debeAplicarConversionCuandoUnidadesDiferentes() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(5)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idUnidadConsumo(6)
                .isMultiplo("1")
                .valorConv(2)
                .stock(BigDecimal.valueOf(1000))
                .build();

        // When
        StepVerifier.create(adapter.aplicarConversion(detalle, infoConversion))
                .assertNext(resultado -> {
                    assertThat(resultado.getArticulo().getIdUnidadSalida()).isEqualTo(6);
                    assertThat(resultado.getArticulo().getIs_multiplo()).isEqualTo("1");
                    assertThat(resultado.getArticulo().getValor_conv()).isEqualTo(2);
                    assertThat(resultado.getArticulo().getStock()).isEqualTo(BigDecimal.valueOf(1000));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe usar idUnidad como idUnidadSalida cuando son iguales")
    void debeUsarIdUnidadCuandoSonIguales() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(6)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idUnidadConsumo(6)
                .isMultiplo("1")
                .valorConv(1)
                .stock(BigDecimal.valueOf(500))
                .build();

        // When
        StepVerifier.create(adapter.aplicarConversion(detalle, infoConversion))
                .assertNext(resultado -> {
                    assertThat(resultado.getArticulo().getIdUnidadSalida()).isEqualTo(6);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idUnidad es null")
    void debeLanzarExcepcionCuandoIdUnidadEsNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(null)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idUnidadConsumo(6)
                .build();

        // When & Then
        StepVerifier.create(adapter.aplicarConversion(detalle, infoConversion))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("ID de unidad no puede ser null para el detalle 1"))
                .verify();
    }

    @Test
    @DisplayName("Debe buscar información de conversión exitosamente")
    void debeBuscarInfoConversionExitosamente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        ArticuloInventory articuloEntity = ArticuloInventory.builder()
                .idArticulo(100)
                .idUnidadConsumo(6)
                .stock(BigDecimal.valueOf(1000))
                .build();

        when(articuloRepository.getInfoConversionArticulo(5, 100))
                .thenReturn(Mono.just(articuloEntity));

        // When
        StepVerifier.create(adapter.buscarInfoConversion(detalle, ordenEgreso))
                .assertNext(resultado -> {
                    assertThat(resultado.getIdArticulo()).isEqualTo(100);
                    assertThat(resultado.getIdUnidadConsumo()).isEqualTo(6);
                    assertThat(resultado.getStock()).isEqualTo(BigDecimal.valueOf(1000));
                })
                .verifyComplete();

        verify(articuloRepository).getInfoConversionArticulo(5, 100);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando detalle es null")
    void debeLanzarExcepcionCuandoDetalleEsNull() {
        // Given
        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder().build();

        // When & Then
        StepVerifier.create(adapter.buscarInfoConversion(null, ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("El detalle no puede ser null"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando orden de egreso es null")
    void debeLanzarExcepcionCuandoOrdenEgresoEsNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder().build();

        // When & Then
        StepVerifier.create(adapter.buscarInfoConversion(detalle, null))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("La orden de egreso no puede ser null"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando almacén origen es null")
    void debeLanzarExcepcionCuandoAlmacenOrigenEsNull() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()  // ✅ Agregar artículo válido
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(null)  // ✅ Este es el que debe fallar
                .build();

        // When & Then
        StepVerifier.create(adapter.buscarInfoConversion(detalle, ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("Almacén origen no puede ser null para la orden 1"))
                .verify();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra información de conversión")
    void debeLanzarExcepcionCuandoNoEncuentraInfoConversion() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenEgreso = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        when(articuloRepository.getInfoConversionArticulo(5, 100))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.buscarInfoConversion(detalle, ordenEgreso))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException &&
                                error.getMessage().contains("No se encontró información de conversión para el artículo: 100"))
                .verify();
    }

    @Test
    @DisplayName("Debe procesar entrega y conversión exitosamente")
    void debeProcesarEntregaYConversionExitosamente() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(5)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idArticulo(100)
                .idUnidadConsumo(6)
                .isMultiplo("1")
                .valorConv(2)
                .stock(BigDecimal.valueOf(1000))
                .build();

        when(articuloRepository.getInfoConversionArticulo(5, 100))
                .thenReturn(Mono.just(infoConversion));

        // When
        StepVerifier.create(adapter.procesarEntregaYConversion(detalle, ordenSalida))
                .assertNext(resultado -> {
                    assertThat(resultado.getArticulo().getIdUnidadSalida()).isEqualTo(6);
                    assertThat(resultado.getArticulo().getIs_multiplo()).isEqualTo("1");
                    assertThat(resultado.getArticulo().getValor_conv()).isEqualTo(2);
                    assertThat(resultado.getArticulo().getStock()).isEqualTo(BigDecimal.valueOf(1000));
                })
                .verifyComplete();

        verify(articuloRepository).getInfoConversionArticulo(5, 100);
    }

    @Test
    @DisplayName("Debe fallar cuando buscarInfoConversion falla")
    void debeFallarCuandoBuscarInfoConversionFalla() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        when(articuloRepository.getInfoConversionArticulo(5, 100))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.procesarEntregaYConversion(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException &&
                                error.getMessage().contains("No se encontró información de conversión para el artículo: 100"))
                .verify();
    }

    @Test
    @DisplayName("Debe fallar cuando aplicarConversion falla")
    void debeFallarCuandoAplicarConversionFalla() {
        // Given
        DetalleEgresoDTO detalle = DetalleEgresoDTO.builder()
                .id(1L)
                .idUnidad(null) // ✅ Esto causará fallo en aplicarConversion
                .articulo(Articulo.builder()
                        .id(100)
                        .build())
                .build();

        OrdenEgresoDTO ordenSalida = OrdenEgresoDTO.builder()
                .id(1L)
                .almacenOrigen(Almacen.builder()
                        .idAlmacen(5)
                        .build())
                .build();

        ArticuloInventory infoConversion = ArticuloInventory.builder()
                .idArticulo(100)
                .idUnidadConsumo(6)
                .build();

        when(articuloRepository.getInfoConversionArticulo(5, 100))
                .thenReturn(Mono.just(infoConversion));

        // When & Then
        StepVerifier.create(adapter.procesarEntregaYConversion(detalle, ordenSalida))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("ID de unidad no puede ser null para el detalle 1"))
                .verify();
    }

    // ✅ Clase interna para testing
    private static class TestableBaseInventarioAdapter extends BaseInventarioAdapter {

        public TestableBaseInventarioAdapter(ArticuloAlmacenRepository articuloRepository) {
            super(articuloRepository);
        }

        // ✅ Exponer método protegido para testing
        @Override
        public Mono<DetalleEgresoDTO> aplicarConversion(DetalleEgresoDTO detalle, ArticuloInventory infoConversion) {
            return super.aplicarConversion(detalle, infoConversion);
        }

        @Override
        public Mono<ArticuloInventory> buscarInfoConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso) {
            return super.buscarInfoConversion(detalle, ordenEgreso);
        }

        @Override
        public Mono<DetalleEgresoDTO> procesarEntregaYConversion(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenEgreso) {
            return super.procesarEntregaYConversion(detalle, ordenEgreso);
        }
    }
}
