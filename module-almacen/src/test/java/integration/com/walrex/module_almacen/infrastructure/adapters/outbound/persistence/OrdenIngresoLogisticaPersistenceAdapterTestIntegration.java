package integration.com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import integration.com.walrex.module_almacen.config.AbstractPostgreSQLContainerTest;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest(
//    classes = com.walrex.module_almacen.ModuleAlmacenApplication.class
//)
//@ActiveProfiles("test-almacenes")
public class OrdenIngresoLogisticaPersistenceAdapterTestIntegration extends AbstractPostgreSQLContainerTest {

    @Autowired
    private OrdenIngresoAdapterFactory adapterFactory;

    // Inyectar los repositories directamente
    @Autowired
    private OrdenIngresoRepository ordenIngresoRepository;

    @Autowired
    private DetailsIngresoRepository detalleRepository;

    @Autowired
    private KardexRepository kardexRepository;

    // Datos comunes para las pruebas
    private final String serieDocumento = "F001";
    private final String numeroDocumento = "1120";

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

    // Orden de ingreso base para las pruebas
    private OrdenIngreso crearOrdenIngresoBase() {
        return OrdenIngreso.builder()
            .idCliente(86)
            .motivo(Motivo.builder().idMotivo(4).descMotivo("COMPRAS").build())
            .comprobante(1)
            .codSerie("F001")
            .nroComprobante("1120")
            .fechaIngreso(LocalDate.parse("2025-03-31"))
            .fechaComprobante(LocalDate.parse("2025-03-31"))
            .observacion("")
            .almacen(Almacen.builder().idAlmacen(1).build())
            .idOrdenCompra(13232)
            .detalles(Collections.emptyList())
            .build();
    }

    //@BeforeEach
    void setUp() {
        // Limpiar datos entre pruebas
        kardexRepository.deleteAll().block();
    }

    //@Test
    //@DisplayName("Registro correcto orden ingreso con su detalles y kardex")
    void debeRegistrarOrdenConDetallesCorrectamente() {
        // Arrange
        OrdenIngreso ordenIngreso = crearOrdenIngresoBase();
        ordenIngreso.setDetalles(List.of(crearDetalleOrdenIngreso()));

        // Act & Assert
        StepVerifier.create(
                adapterFactory.getAdapter(TipoOrdenIngreso.LOGISTICA_GENERAL)
                    .flatMap(adapter->adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                    .doOnNext(result -> System.out.println("✅ Orden guardada: " + result))
                    .doOnError(error -> System.err.println("❌ Error: " + error))
            )
            .expectNextCount(1) // Solo verifica que emite un elemento
            .verifyComplete();

        kardexRepository.findByIdArticuloAndIdAlmacen(289, 1)
            .as(StepVerifier::create)
            .expectNextMatches(kardex -> {
                // Verificar los datos específicos del Kardex
                boolean detallesKardexOk =
                        kardex.getTipo_movimiento() == 1 && // 1 = ingreso
                                kardex.getId_articulo() == 289 &&
                                kardex.getId_almacen() == 1 &&
                                // Comprobar que el detalle incluye el código de ingreso y el motivo
                                kardex.getDetalle().contains("COMPRAS");
                // Verificar conversión: 240 * 10^3 = 240000 (factor conv = 3)
                boolean conversionCorrecta =
                        kardex.getSaldoLote().compareTo(new BigDecimal("240000.000000")) == 0;

                // Stock original (544982.9) + cantidad convertida (240000) = 784982.9
                boolean stockCorrecto =
                        kardex.getSaldo_actual().compareTo(new BigDecimal("784982.900000")) == 0;

                return detallesKardexOk && conversionCorrecta && stockCorrecto;
            })
            .verifyComplete();
    }

    //@Test
    //@DisplayName("Debe devolver error si la lista de detalles está vacía")
    void debeRetornarErrorSiDetallesEstaVacio() {
        // Arrange
        OrdenIngreso ordenIngreso = crearOrdenIngresoBase();
        ordenIngreso.setDetalles(Collections.emptyList());

        // Act & Assert
        /*
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                        .isInstanceOf(ResponseStatusException.class)
                        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                        .hasMessageContaining("La orden de ingreso debe tener al menos un detalle");
                })
                .verify();
         */
    }

    //@Test
    //@DisplayName("Debe devolver error si el ID de almacén está vacío")
    void debeRetornarErrorSiAlmacenEstaVacio() {
        // Arrange
        OrdenIngreso ordenIngreso = crearOrdenIngresoBase();
        // Establecer almacén sin ID
        ordenIngreso.setAlmacen(Almacen.builder().build());
        ordenIngreso.setDetalles(List.of(crearDetalleOrdenIngreso()));

        // Act & Assert
        /*
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectErrorSatisfies(error -> {
                    // Log detallado del error para diagnóstico
                    System.out.println("Error capturado: " + error.getClass().getName());
                    System.out.println("Mensaje: " + error.getMessage());
                    if (error.getCause() != null) {
                        System.out.println("Causa: " + error.getCause().getClass().getName());
                        System.out.println("Mensaje causa: " + error.getCause().getMessage());
                    }
                    // 1. Verificar que el error se ha traducido a OrdenIngresoException
                    assertTrue(error instanceof OrdenIngresoException,
                            "El error debe ser de tipo OrdenIngresoException, pero es: " +
                                    error.getClass().getName());

                    // 2. Extraer y verificar la causa raíz
                    Throwable rootCause = error;
                    while (rootCause.getCause() != null) {
                        rootCause = rootCause.getCause();
                    }

                    // 3. Verificar el mensaje de error
                    String errorMessage = error.getMessage().toLowerCase();
                    String rootCauseMessage = rootCause.getMessage() != null ?
                            rootCause.getMessage().toLowerCase() : "";

                    // 4. Comprobar si alguno de los mensajes contiene indicaciones sobre el problema
                    boolean mensajeContieneAlmacen =
                            errorMessage.contains("almac") || rootCauseMessage.contains("almac");
                    boolean mensajeContieneNull =
                            errorMessage.contains("null") || rootCauseMessage.contains("null") ||
                                    errorMessage.contains("vacío") || rootCauseMessage.contains("vacío") ||
                                    errorMessage.contains("vacio") || rootCauseMessage.contains("vacio");
                    boolean mensajeContieneConstraint =
                            errorMessage.contains("constraint") || rootCauseMessage.contains("constraint") ||
                                    errorMessage.contains("violation") || rootCauseMessage.contains("violation") ||
                                    errorMessage.contains("exception") || rootCauseMessage.contains("exception");

                    assertTrue(mensajeContieneAlmacen || mensajeContieneNull || mensajeContieneConstraint,
                            "El mensaje debe indicar el problema con el almacén: " +
                                    error.getMessage() + " / Causa raíz: " + rootCauseMessage);

                    // 5. Si se puede identificar el tipo específico de error de PostgreSQL
                    if (rootCause.getClass().getName().contains("R2dbc") &&
                            rootCauseMessage.contains("23")) {  // Los errores 23xxx son de integridad
                        System.out.println("Detectado error PostgreSQL de integridad");
                    }
                })
                .verify();
         */

        // Verificar que no se hayan creado registros
        StepVerifier.create(ordenIngresoRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    //@Test
    //@DisplayName("Debe almacenar correctamente los cálculos en Kardex")
    void debeAlmacenarCalculosCorrectosEnKardex() {
        // Arrange - Crear orden con detalle que requiere conversión
        OrdenIngreso ordenIngreso = crearOrdenIngresoBase();

        // Detalle especial para probar los cálculos
        DetalleOrdenIngreso detalle = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(289)
                        .codigo("PQ00080")
                        .descripcion("SILTEX CONC")
                        .valor_conv(3) // Factor de conversión 10^3
                        .stock(new BigDecimal("544982.900000"))
                        .build())
                .idTipoProducto(1)
                .idTipoProductoFamilia(10)
                .idUnidad(1)  // Unidad de ingreso
                .idUnidadSalida(2)  // Unidad de salida diferente
                .idMoneda(1)
                .cantidad(new BigDecimal("240.00"))
                .excentoImp(false)
                .costo(new BigDecimal("50.00"))
                .montoTotal(new BigDecimal("12000.00"))
                .detallesRollos(Collections.emptyList())
                .build();

        ordenIngreso.setDetalles(List.of(detalle));

        // Act
        /*
        adapter.guardarOrdenIngresoLogistica(ordenIngreso)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
         */

        // Assert - Verificar cálculos en Kardex
        kardexRepository.findByIdArticuloAndIdAlmacen(289, 1)
                .as(StepVerifier::create)
                .expectNextMatches(kardex -> {
                    // Verificar datos básicos
                    boolean datosBasicosOk =
                            kardex.getTipo_movimiento() == 1 &&
                                    kardex.getId_articulo() == 289 &&
                                    kardex.getId_almacen() == 1;

                    // Calcular valores esperados
                    BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, 3));
                    BigDecimal cantidadIngresada = new BigDecimal("240.00");
                    BigDecimal cantidadConvertida = cantidadIngresada.multiply(factorConversion)
                            .setScale(6, RoundingMode.HALF_UP);
                    BigDecimal stockOriginal = new BigDecimal("544982.900000");
                    BigDecimal stockFinal = stockOriginal.add(cantidadConvertida)
                            .setScale(6, RoundingMode.HALF_UP);
                    BigDecimal costoUnitario = new BigDecimal("50.00");
                    BigDecimal valorTotal = cantidadIngresada.multiply(costoUnitario)
                            .setScale(2, RoundingMode.HALF_UP);

                    // Comparar con valores reales
                    boolean calculosCorrectos =
                            kardex.getCantidad().compareTo(cantidadIngresada) == 0 &&
                                    kardex.getCosto().compareTo(costoUnitario) == 0 &&
                                    kardex.getValorTotal().compareTo(valorTotal) == 0 &&
                                    kardex.getSaldoLote().compareTo(cantidadConvertida) == 0 &&
                                    kardex.getSaldo_actual().compareTo(stockFinal) == 0;

                    // Verificar relación con documento
                    boolean relacionDocumentoOk = kardex.getId_documento() != null &&
                            kardex.getId_detalle_documento() != null;

                    // Log para depuración
                    if (!calculosCorrectos) {
                        System.out.println("Cantidad ingresada esperada: " + cantidadIngresada +
                                ", real: " + kardex.getCantidad());
                        System.out.println("Costo unitario esperado: " + costoUnitario +
                                ", real: " + kardex.getCosto());
                        System.out.println("Valor total esperado: " + valorTotal +
                                ", real: " + kardex.getValorTotal());
                        System.out.println("Saldo lote esperado: " + cantidadConvertida +
                                ", real: " + kardex.getSaldoLote());
                        System.out.println("Saldo actual esperado: " + stockFinal +
                                ", real: " + kardex.getSaldo_actual());
                    }

                    return datosBasicosOk && calculosCorrectos && relacionDocumentoOk;
                })
                .verifyComplete();
    }

    //@Test
    //@DisplayName("Debe manejar correctamente excepciones R2dbcException")
    void debeCapturarExcepcionesR2dbc() {
        // Arrange
        OrdenIngreso ordenIngreso = crearOrdenIngresoBase();

        // Crear detalle con datos que provocarán error en la base de datos
        // Por ejemplo, un ID de artículo inexistente que viole una restricción de clave foránea
        DetalleOrdenIngreso detalleConErrorFK = DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                        .id(999999) // ID inexistente - provocará violación de foreign key
                        .codigo("INVALID")
                        .descripcion("Artículo inexistente")
                        .valor_conv(1)
                        .stock(BigDecimal.ZERO)
                        .build())
                .idTipoProducto(1)
                .idTipoProductoFamilia(10)
                .idUnidad(1)
                .idUnidadSalida(1)
                .idMoneda(1)
                .cantidad(new BigDecimal("10.00"))
                .excentoImp(false)
                .costo(new BigDecimal("100.00"))
                .montoTotal(new BigDecimal("1000.00"))
                .detallesRollos(Collections.emptyList())
                .build();

        ordenIngreso.setDetalles(List.of(detalleConErrorFK));

        // Act & Assert
        /*
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenIngreso))
                .expectErrorSatisfies(error -> {
                    // Verificar que el error es del tipo OrdenIngresoException que envuelve un R2dbcException
                    assertTrue(error instanceof OrdenIngresoException,
                            "El error debe ser de tipo OrdenIngresoException");

                    // Si el error tiene una causa, verificar que sea R2dbcException o derivado
                    if (error.getCause() != null) {
                        boolean isR2dbcError = false;
                        Throwable cause = error.getCause();

                        // Buscar en la cadena de causas
                        while (cause != null && !isR2dbcError) {
                            isR2dbcError = cause.getClass().getName().contains("R2dbc") ||
                                    cause.getClass().getName().contains("DataIntegrityViolation");
                            cause = cause.getCause();
                        }

                        assertTrue(isR2dbcError,
                                "La causa raíz debe ser un error R2DBC o de integridad de datos");
                    }

                    // Verificar que el mensaje de error sea comprensible para el usuario
                    String errorMessage = error.getMessage().toLowerCase();
                    assertTrue(errorMessage.contains("error") ||
                                    errorMessage.contains("registrar") ||
                                    errorMessage.contains("detalle"),
                            "El mensaje debe ser comprensible: " + error.getMessage());

                    // Verificar que no se exponga información técnica de la base de datos en el mensaje principal
                    assertFalse(errorMessage.contains("constraint") ||
                                    errorMessage.contains("foreign key") ||
                                    errorMessage.contains("sql"),
                            "El mensaje no debe exponer detalles técnicos: " + error.getMessage());

                    // Log para diagnóstico
                    System.out.println("Error capturado (esperado en esta prueba): " + error.getMessage());
                    if (error.getCause() != null) {
                        System.out.println("Causa: " + error.getCause().getClass().getName() +
                                ": " + error.getCause().getMessage());
                    }
                })
                .verify();
         */

        // Verificar que no queden datos parciales (la transacción debe haberse revertido)
        StepVerifier.create(ordenIngresoRepository.count())
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(detalleRepository.count())
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(kardexRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }
}
