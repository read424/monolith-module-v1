package com.walrex.module_almacen.infrastructure.adapters.outbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionItemProjection;

/**
 * Test unitario para GuiaRemisionDTOMapper
 * 
 * Verifica que el mapper convierta correctamente:
 * - GuiaRemisionGeneradaDTO a CreateGuiaRemisionRemitenteMessage
 * - GuiaRemisionItemProjection a ItemGuiaRemisionRemitenteMessage
 * - Listas de items
 */
@DisplayName("GuiaRemisionDTOMapper Tests")
class GuiaRemisionDTOMapperTest {

    private GuiaRemisionDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(GuiaRemisionDTOMapper.class);
    }

    @Test
    @DisplayName("✅ Debe convertir GuiaRemisionGeneradaDTO a CreateGuiaRemisionRemitenteMessage")
    void testToCreateGuiaRemisionRemitenteMessage() {
        // Given
        GuiaRemisionGeneradaDTO guiaDTO = GuiaRemisionGeneradaDTO.builder()
                .idOrdenSalida(123L)
                .codigoSalida("SAL-2024-001")
                .fechaEntrega(LocalDate.of(2024, 1, 15))
                .idEmpresaTransp(1)
                .idModalidad(1)
                .idTipDocChofer(1)
                .numDocChofer("12345678")
                .numPlaca("ABC-123")
                .idLlegada(1)
                .idComprobante(1)
                .entregado(1)
                .status(1)
                .idUsuario(1)
                .build();

        List<ItemGuiaRemisionRemitenteMessage> items = List.of(
                createItemMessage(1, 123, 10.0f, 100.0f, 1000.0f, 1, 5.0f, 1, 1),
                createItemMessage(2, 123, 5.0f, 200.0f, 1000.0f, 2, 3.0f, 1, 1));

        // When
        CreateGuiaRemisionRemitenteMessage result = mapper.toCreateGuiaRemisionRemitenteMessage(guiaDTO, items);

        // Then
        assertNotNull(result);
        assertEquals("2024-01-15", result.getFechaEmision());
        assertEquals(1, result.getIdCliente()); // Valor por defecto
        assertEquals(1, result.getIdMotivo()); // Valor por defecto
        assertEquals(2, result.getDetailItems().size());
        assertEquals(1, result.getDetailItems().get(0).getIdProducto());
        assertEquals(2, result.getDetailItems().get(1).getIdProducto());
    }

    @Test
    @DisplayName("✅ Debe convertir GuiaRemisionItemProjection a ItemGuiaRemisionRemitenteMessage")
    void testToItemGuiaRemisionRemitenteMessage() {
        // Given
        GuiaRemisionItemProjection itemProjection = new GuiaRemisionItemProjection(
                1, // idProducto
                123, // idOrdensalida
                10.0f, // cantidad
                100.0f, // precio
                1000.0f, // subtotal
                1, // idDetalleOrden
                5.0f, // peso
                1, // idUnidad
                1 // tipoServicio
        );

        // When
        ItemGuiaRemisionRemitenteMessage result = mapper.toItemGuiaRemisionRemitenteMessage(itemProjection);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getIdProducto());
        assertEquals(123, result.getIdOrdensalida());
        assertEquals(10.0f, result.getCantidad(), 0.001f);
        assertEquals(100.0f, result.getPrecio(), 0.001f);
        assertEquals(1000.0f, result.getSubtotal(), 0.001f);
        assertEquals(1, result.getIdDetalleOrden());
        assertEquals(5.0f, result.getPeso(), 0.001f);
        assertEquals(1, result.getIdUnidad());
        assertEquals(1, result.getTipoServicio());
    }

    @Test
    @DisplayName("✅ Debe convertir lista de GuiaRemisionItemProjection a lista de ItemGuiaRemisionRemitenteMessage")
    void testToItemGuiaRemisionRemitenteMessageList() {
        // Given
        List<GuiaRemisionItemProjection> itemsProjection = List.of(
                new GuiaRemisionItemProjection(1, 123, 10.0f, 100.0f, 1000.0f, 1, 5.0f, 1, 1),
                new GuiaRemisionItemProjection(2, 123, 5.0f, 200.0f, 1000.0f, 2, 3.0f, 1, 1),
                new GuiaRemisionItemProjection(3, 123, 15.0f, 50.0f, 750.0f, 3, 2.0f, 1, 1));

        // When
        List<ItemGuiaRemisionRemitenteMessage> result = mapper.toItemGuiaRemisionRemitenteMessageList(itemsProjection);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getIdProducto());
        assertEquals(2, result.get(1).getIdProducto());
        assertEquals(3, result.get(2).getIdProducto());
    }

    @Test
    @DisplayName("✅ Debe convertir con datos completos desde BD")
    void testToCreateGuiaRemisionRemitenteMessageWithData() {
        // Given
        GuiaRemisionGeneradaDTO guiaDTO = GuiaRemisionGeneradaDTO.builder()
                .idOrdenSalida(123L)
                .fechaEntrega(LocalDate.of(2024, 1, 15))
                .build();

        List<ItemGuiaRemisionRemitenteMessage> items = List.of(
                createItemMessage(1, 123, 10.0f, 100.0f, 1000.0f, 1, 5.0f, 1, 1));

        Integer idCliente = 456;
        Integer idMotivo = 789;

        // When
        CreateGuiaRemisionRemitenteMessage result = mapper.toCreateGuiaRemisionRemitenteMessageWithData(
                guiaDTO, idCliente, idMotivo, items);

        // Then
        assertNotNull(result);
        assertEquals("2024-01-15", result.getFechaEmision());
        assertEquals(456, result.getIdCliente());
        assertEquals(789, result.getIdMotivo());
        assertEquals(1, result.getDetailItems().size());
    }

    @Test
    @DisplayName("✅ Debe manejar fecha nula correctamente")
    void testLocalDateToStringWithNull() {
        // Given
        GuiaRemisionGeneradaDTO guiaDTO = GuiaRemisionGeneradaDTO.builder()
                .idOrdenSalida(123L)
                .fechaEntrega(null) // Fecha nula
                .build();

        List<ItemGuiaRemisionRemitenteMessage> items = List.of();

        // When
        CreateGuiaRemisionRemitenteMessage result = mapper.toCreateGuiaRemisionRemitenteMessage(guiaDTO, items);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFechaEmision()); // String vacío para fecha nula
    }

    /**
     * Helper para crear ItemGuiaRemisionRemitenteMessage
     */
    private ItemGuiaRemisionRemitenteMessage createItemMessage(
            Integer idProducto, Integer idOrdensalida, Float cantidad, Float precio,
            Float subtotal, Integer idDetalleOrden, Float peso, Integer idUnidad, Integer tipoServicio) {

        return ItemGuiaRemisionRemitenteMessage.newBuilder()
                .setIdProducto(idProducto)
                .setIdOrdensalida(idOrdensalida)
                .setCantidad(cantidad)
                .setPrecio(precio)
                .setSubtotal(subtotal)
                .setIdDetalleOrden(idDetalleOrden)
                .setPeso(peso)
                .setIdUnidad(idUnidad)
                .setTipoServicio(tipoServicio)
                .build();
    }
}