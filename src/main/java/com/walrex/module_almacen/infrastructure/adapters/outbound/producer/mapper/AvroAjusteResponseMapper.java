package com.walrex.module_almacen.infrastructure.adapters.outbound.producer.mapper;

import com.walrex.avro.schemas.*;
import com.walrex.module_almacen.domain.model.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvroAjusteResponseMapper {
    AvroAjusteResponseMapper INSTANCE = Mappers.getMapper(AvroAjusteResponseMapper.class);

    /**
     * Convierte ResponseAjusteInventoryDTO a AjustInventaryResponseMessage
     */
    @Mapping(source = "message", target = "message")
    @Mapping(source = "transactionId", target = "transactionId")
    @Mapping(source = "result_ingresos", target = "ingreso")
    @Mapping(source = "result_egresos", target = "engreso")
    AjustInventaryResponseMessage toAvroResponse(ResponseAjusteInventoryDTO dto);

    /**
     * Convierte ResultAjustIngresoDTO a IngresoResponseMessage
     */
    @Mapping(source = "id", target = "idOrdenIngreso")
    @Mapping(source = "num_saved", target = "numSaved")
    @Mapping(source = "details", target = "details")
    IngresoResponseMessage toIngresoResponse(ResultAjustIngresoDTO dto);

    /**
     * Convierte ResultAjustEgresoDTO a EgresoResponseMessage
     */
    @Mapping(source = "id", target = "idOrdenSalida")
    @Mapping(source = "num_saved", target = "numSaved")
    @Mapping(source = "details", target = "details")
    EgresoResponseMessage toEgresoResponse(ResultAjustEgresoDTO dto);

    /**
     * Convierte ItemResultSavedDTO a ItemArticleIngresoResponse
     */
    @Mapping(source = "id", target = "idDetordeningreso")
    @Mapping(source = "id_articulo", target = "idArticulo")
    @Mapping(source = "id_lote", target = "idLote")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precio", target = "precio")
    @Mapping(source = "observacion", target = "observacion", defaultValue = "")
    ItemArticleIngresoResponse toItemArticleIngresoResponse(ItemResultSavedDTO dto);

    /**
     * Convierte ItemArticuloEgreso a ItemArticleEgresoResponse
     */
    @Mapping(source = "id_detalle_orden", target = "idDetalleOrden")
    @Mapping(source = "id_articulo", target = "idArticulo")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "a_lote", target = "lotes")
    ItemArticleEgresoResponse toItemArticleEgresoResponse(ItemArticuloEgreso dto);

    /**
     * Convierte LoteDTO a ItemLoteResponse
     */
    @Mapping(source = "idsalida_ote", target = "idSalidaLote")
    @Mapping(source = "id_lote", target = "idLote")
    @Mapping(source = "cantidad", target = "cantidad")
    ItemLoteResponse toItemLoteResponse(LoteDTO dto);

    /**
     * Convierte lista de ItemResultSavedDTO a lista de ItemArticleIngresoResponse
     */
    List<ItemArticleIngresoResponse> toItemArticleIngresoResponseList(List<ItemResultSavedDTO> dtos);

    /**
     * Convierte lista de ItemArticuloEgreso a lista de ItemArticleEgresoResponse
     */
    List<ItemArticleEgresoResponse> toItemArticleEgresoResponseList(List<ItemArticuloEgreso> dtos);

    /**
     * Convierte lista de LoteDTO a lista de ItemLoteResponse
     */
    List<ItemLoteResponse> toItemLoteResponseList(List<LoteDTO> dtos);

    /**
     * Maneja la conversión de valores nulos o vacíos
     */
    @AfterMapping
    default void handleEmptyValues(@MappingTarget AjustInventaryResponseMessage target, ResponseAjusteInventoryDTO source) {
        // Si el mensaje no está presente, establecer un valor predeterminado
        if (target.getMessage() == null) {
            target.setMessage("");
        }

        // Si el transactionId no está presente, establecer un valor predeterminado
        if (target.getTransactionId() == null) {
            target.setTransactionId("");
        }
    }

    /**
     * Maneja la conversión de Double a float para cantidades y precios
     */
    @AfterMapping
    default void handleDoubleToFloat(@MappingTarget ItemArticleIngresoResponse target, ItemResultSavedDTO source) {
        if (source.getCantidad() != null) {
            target.setCantidad(source.getCantidad().floatValue());
        }

        if (source.getPrecio() != null) {
            target.setPrecio(source.getPrecio().floatValue());
        }

        if (target.getObservacion() == null) {
            target.setObservacion("");
        }
    }

    /**
     * Maneja la conversión de Double a float para cantidades en egresos
     */
    @AfterMapping
    default void handleDoubleToFloat(@MappingTarget ItemArticleEgresoResponse target, ItemArticuloEgreso source) {
        if (source.getCantidad() != null) {
            target.setCantidad(source.getCantidad().floatValue());
        }
    }

    /**
     * Maneja la conversión de Double a float para lotes
     */
    @AfterMapping
    default void handleDoubleToFloat(@MappingTarget ItemLoteResponse target, LoteDTO source) {
        if (source.getCantidad() != null) {
            target.setCantidad(source.getCantidad().floatValue());
        }
    }
}