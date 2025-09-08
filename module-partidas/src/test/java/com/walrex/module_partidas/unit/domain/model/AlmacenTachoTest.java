package com.walrex.module_partidas.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.*;

import com.walrex.module_partidas.domain.model.AlmacenTacho;

/**
 * Tests unitarios para el modelo de dominio AlmacenTacho
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@DisplayName("AlmacenTacho Domain Model Tests")
class AlmacenTachoTest {

    private AlmacenTacho almacenTacho;
    private final LocalDateTime fechaRegistro = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

    @BeforeEach
    void setUp() {
        almacenTacho = AlmacenTacho.builder()
                .idOrdeningreso(307874)
                .idCliente(138)
                .razonSocial("HUANCATEX S.A.C.")
                .noAlias("HUANCATEX")
                .fecRegistro(LocalDateTime.of(2025, 8, 19, 3, 30, 35))
                .codIngreso("ALGT-I46331")
                .idDetordeningreso(330449)
                .idPartida(55509)
                .codPartida("PA25-0048661")
                .cntRollos(18)
                .codReceta("RT25-25388")
                .noColores("NEGRO")
                .idTipoTenido(5)
                .descTenido("DISPERSO")
                .build();
    }

    @Nested
    @DisplayName("Constructor y Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Debería crear AlmacenTacho con todos los campos correctamente")
        void shouldCreateAlmacenTachoWithAllFields() {
            // Assert
            assertNotNull(almacenTacho);
            assertEquals(307874, almacenTacho.getIdOrdeningreso());
            assertEquals(138, almacenTacho.getIdCliente());
            assertEquals("HUANCATEX S.A.C.", almacenTacho.getRazonSocial());
            assertEquals("HUANCATEX", almacenTacho.getNoAlias());
            assertEquals(LocalDateTime.of(2025, 8, 19, 3, 30, 35), almacenTacho.getFecRegistro());
            assertEquals("ALGT-I46331", almacenTacho.getCodIngreso());
            assertEquals(330449, almacenTacho.getIdDetordeningreso());
            assertEquals(55509, almacenTacho.getIdPartida());
            assertEquals("PA25-0048661", almacenTacho.getCodPartida());
            assertEquals(18, almacenTacho.getCntRollos());
            assertEquals("RT25-25388", almacenTacho.getCodReceta());
            assertEquals("NEGRO", almacenTacho.getNoColores());
            assertEquals(5, almacenTacho.getIdTipoTenido());
            assertEquals("DISPERSO", almacenTacho.getDescTenido());
        }
    }

    @Nested
    @DisplayName("Equals y HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Debería ser igual a sí mismo")
        void shouldBeEqualToItself() {
            assertEquals(almacenTacho, almacenTacho);
        }

        @Test
        @DisplayName("Debería ser igual a otro objeto con los mismos valores")
        void shouldBeEqualToObjectWithSameValues() {
            AlmacenTacho otroAlmacenTacho = AlmacenTacho.builder()
                    .idOrdeningreso(307874)
                    .idCliente(138)
                    .razonSocial("HUANCATEX S.A.C.")
                    .noAlias("HUANCATEX")
                    .fecRegistro(LocalDateTime.of(2025, 8, 19, 3, 30, 35))
                    .codIngreso("ALGT-I46331")
                    .idDetordeningreso(330449)
                    .idPartida(55509)
                    .codPartida("PA25-0048661")
                    .cntRollos(18)
                    .codReceta("RT25-25388")
                    .noColores("NEGRO")
                    .idTipoTenido(5)
                    .descTenido("DISPERSO")
                    .build();

            assertEquals(almacenTacho, otroAlmacenTacho);
            assertEquals(almacenTacho.hashCode(), otroAlmacenTacho.hashCode());
        }

        @Test
        @DisplayName("No debería ser igual a objeto con valores diferentes")
        void shouldNotBeEqualToObjectWithDifferentValues() {
            AlmacenTacho otroAlmacenTacho = AlmacenTacho.builder()
                    .idOrdeningreso(9999) // Diferente ID
                    .idCliente(2001)
                    .razonSocial("Empresa Textil ABC")
                    .noAlias("ABC")
                    .fecRegistro(fechaRegistro)
                    .codIngreso("ING-001")
                    .idDetordeningreso(3001)
                    .idPartida(4001)
                    .codPartida("PART-001")
                    .cntRollos(25)
                    .codReceta("REC-001")
                    .noColores("Azul")
                    .idTipoTenido(5001)
                    .descTenido("Teñido Industrial")
                    .build();

            assertNotEquals(almacenTacho, otroAlmacenTacho);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Debería contener información relevante en toString")
        void shouldContainRelevantInformationInToString() {
            String toString = almacenTacho.toString();

            assertTrue(toString.contains("307874")); // idOrdeningreso
            assertTrue(toString.contains("HUANCATEX S.A.C.")); // razonSocial
            assertTrue(toString.contains("ALGT-I46331")); // codIngreso
            assertTrue(toString.contains("PA25-0048661")); // codPartida
        }
    }
}
