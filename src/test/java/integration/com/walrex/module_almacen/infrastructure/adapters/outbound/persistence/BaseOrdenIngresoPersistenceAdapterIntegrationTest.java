package integration.com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import integration.com.walrex.module_almacen.config.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = com.walrex.module_almacen.ModuleAlmacenTestApplication.class
)
@ActiveProfiles("test-almacen")
public class BaseOrdenIngresoPersistenceAdapterIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private OrdenIngresoAdapterFactory adapterFactory;

    @Autowired
    private OrdenIngresoRepository ordenIngresoRepository;

    @Autowired
    private DetailsIngresoRepository detalleRepository;

    @BeforeEach
    void setUp() {
        // Limpiar datos entre pruebas
        detalleRepository.deleteAll().block();
        ordenIngresoRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    @DisplayName("Debería guardar orden de ingreso básica")
    void deberiaGuardarOrdenIngresoBasica() {
        // Given
        OrdenIngreso ordenIngreso = crearOrdenIngresoMinima();

        // When
        Mono<OrdenIngreso> resultado = adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngreso));

        // Then
        StepVerifier.create(resultado)
                .assertNext(ordenGuardada -> {
                    System.out.println("=== DEBUG ===");
                    System.out.println("ID: " + ordenGuardada.getId());
                    System.out.println("Código: '" + ordenGuardada.getCod_ingreso() + "'");
                    System.out.println("Es null? " + (ordenGuardada.getCod_ingreso() == null));
                    System.out.println("Es empty? " + (ordenGuardada.getCod_ingreso() != null && ordenGuardada.getCod_ingreso().isEmpty()));

                    assertThat(ordenGuardada.getId()).isNotNull();
                    assertThat(ordenGuardada.getCod_ingreso()).isNotNull();
                    assertThat(ordenGuardada.getDetalles()).hasSize(1);
                })
                .verifyComplete();

        // Verificar que se guardó en BD
        StepVerifier.create(ordenIngresoRepository.count())
                .expectNext(1L)
                .verifyComplete();
    }

    private OrdenIngreso crearOrdenIngresoMinima() {

        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(289)
                        .descripcion("SILTEX CONC")
                        .stock(BigDecimal.valueOf(100.00))
                        .is_multiplo("1")
                        .valor_conv(3)
                        .build())
                .idUnidad(1)
                .cantidad(BigDecimal.valueOf(10.00))
                .costo(BigDecimal.valueOf(25.50))
                .idMoneda(2)
                .excentoImp(false)
                .build();

        return OrdenIngreso.builder()
                .idCliente(86)
                .fechaIngreso(LocalDate.now())
                .almacen(Almacen.builder()
                        .idAlmacen(1)
                        .build()
                )
                .motivo(Motivo.builder()
                        .idMotivo(4)
                        .descMotivo("COMPRAS")
                        .build()
                )
                .fechaIngreso(LocalDate.now())
                .detalles(List.of(detalle))
                .build();
    }
}
