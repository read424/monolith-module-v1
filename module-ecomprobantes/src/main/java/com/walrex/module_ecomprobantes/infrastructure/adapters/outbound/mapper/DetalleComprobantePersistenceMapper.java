package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.*;

import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_ecomprobantes.domain.model.dto.DetalleComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.DetalleComprobanteEntity;

/**
 * Mapper para convertir ItemGuiaRemisionRemitenteMessage a
 * DetalleComprobanteEntity y DTO
 * 
 * CARACTERÍSTICAS:
 * - Usa MapStruct para mapeo automático
 * - Configurado como componente Spring
 * - Maneja conversiones de tipos específicas (float a BigDecimal)
 * - Aplica valores por defecto para campos requeridos
 * - Incluye mapeo a DTO para uso en servicios
 * - Sigue principios de arquitectura hexagonal
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetalleComprobantePersistenceMapper {

    /**
     * Convierte ItemGuiaRemisionRemitenteMessage a DetalleComprobanteEntity
     * 
     * @param item          Item del mensaje Avro
     * @param idComprobante ID del comprobante padre
     * @return DetalleComprobanteEntity mapeado para R2DBC
     */
    @Mapping(target = "idComprobante", source = "idComprobante")
    @Mapping(source = "item.idProducto", target = "idProducto")
    @Mapping(source = "item.idOrdensalida", target = "idOrdenSalida")
    @Mapping(source = "item.cantidad", target = "cantidad", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "item.precio", target = "precio", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "item.precio", target = "precioOriginal", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "item.subtotal", target = "subtotal", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "item.idDetalleOrden", target = "idDetalleOrden")
    @Mapping(source = "item.peso", target = "peso", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "item.idUnidad", target = "idUnidad")
    @Mapping(source = "item.tipoServicio", target = "idTipoServicio", qualifiedByName = "intToShort")

    // ✅ Campo calculado
    @Mapping(expression = "java(buildObservacion(item))", target = "observacion")

    // ✅ Campos auditoria se manejan automáticamente por R2DBC
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "idDetalleComprobante", ignore = true)

    DetalleComprobanteEntity toEntity(ItemGuiaRemisionRemitenteMessage item, Long idComprobante);

    /**
     * Convierte ItemGuiaRemisionRemitenteMessage a DetalleComprobanteDTO
     * 
     * @param item Item del mensaje Avro
     * @return DetalleComprobanteDTO para uso en servicios
     */
    @Mapping(source = "idProducto", target = "idProducto")
    @Mapping(source = "idOrdensalida", target = "idOrdenSalida")
    @Mapping(source = "cantidad", target = "cantidad", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "precio", target = "precio", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "precio", target = "precioOriginal", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "subtotal", target = "subtotal", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "idDetalleOrden", target = "idDetalleOrden")
    @Mapping(source = "peso", target = "peso", qualifiedByName = "floatToBigDecimal")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "tipoServicio", target = "idTipoServicio", qualifiedByName = "intToShort")

    // ✅ Campo calculado
    @Mapping(expression = "java(buildObservacionDTO(item))", target = "observacion")

    // ✅ ID se asigna después
    @Mapping(target = "idComprobante", ignore = true)
    @Mapping(target = "idDetalleComprobante", ignore = true)

    DetalleComprobanteDTO toDTO(ItemGuiaRemisionRemitenteMessage item);

    /**
     * Convierte lista de items a lista de entidades
     * 
     * @param items         Lista de items del mensaje Avro
     * @param idComprobante ID del comprobante padre
     * @return Lista de DetalleComprobanteEntity
     */
    default List<DetalleComprobanteEntity> toEntityList(List<ItemGuiaRemisionRemitenteMessage> items,
            Long idComprobante) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> toEntity(item, idComprobante))
                .toList();
    }

    /**
     * Convierte lista de items a lista de DTOs
     * 
     * @param items Lista de items del mensaje Avro
     * @return Lista de DetalleComprobanteDTO
     */
    default List<DetalleComprobanteDTO> toDTOList(List<ItemGuiaRemisionRemitenteMessage> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Convierte float a BigDecimal
     * 
     * @param value valor float
     * @return BigDecimal equivalent
     */
    @Named("floatToBigDecimal")
    default BigDecimal floatToBigDecimal(Float value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    /**
     * Convierte int a short
     * 
     * @param value valor int
     * @return short equivalent
     */
    @Named("intToShort")
    default Short intToShort(Integer value) {
        return value != null ? value.shortValue() : (short) 1;
    }

    /**
     * Construye observación del detalle para entidad
     * 
     * @param item Item del mensaje
     * @return Observación formateada
     */
    default String buildObservacion(ItemGuiaRemisionRemitenteMessage item) {
        return String.format("Detalle de guía de remisión - Producto: %d, Orden: %d",
                item.getIdProducto(), item.getIdOrdensalida());
    }

    /**
     * Construye observación del detalle para DTO
     * 
     * @param item Item del mensaje
     * @return Observación formateada
     */
    default String buildObservacionDTO(ItemGuiaRemisionRemitenteMessage item) {
        return String.format("Detalle de guía de remisión - Producto: %d, Orden: %d",
                item.getIdProducto(), item.getIdOrdensalida());
    }
}