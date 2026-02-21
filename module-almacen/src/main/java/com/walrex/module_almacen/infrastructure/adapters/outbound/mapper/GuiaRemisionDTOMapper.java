package com.walrex.module_almacen.infrastructure.adapters.outbound.mapper;

import java.time.LocalDate;
import java.util.List;

import org.mapstruct.*;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionItemProjection;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionDTOMapper {

    @Mapping(target = "idCliente", expression = "java(1)")
    @Mapping(target = "idMotivo", expression = "java(1)")
    @Mapping(source = "guiaDTO.fechaEntrega", target = "fechaEmision", qualifiedByName = "localDateToString")
    @Mapping(source = "items", target = "detailItems")
    CreateGuiaRemisionRemitenteMessage toCreateGuiaRemisionRemitenteMessage(GuiaRemisionGeneradaDTO guiaDTO, List<ItemGuiaRemisionRemitenteMessage> items);

    @Mapping(source = "idProducto", target = "idProducto")
    @Mapping(source = "idOrdensalida", target = "idOrdensalida")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precio", target = "precio")
    @Mapping(source = "subtotal", target = "subtotal")
    @Mapping(source = "idDetalleOrden", target = "idDetalleOrden")
    @Mapping(source = "peso", target = "peso")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "tipoServicio", target = "tipoServicio")
    ItemGuiaRemisionRemitenteMessage toItemGuiaRemisionRemitenteMessage(GuiaRemisionItemProjection item);

    List<ItemGuiaRemisionRemitenteMessage> toItemGuiaRemisionRemitenteMessageList(List<GuiaRemisionItemProjection> itemsProjection);

    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idMotivo", target = "idMotivo")
    @Mapping(source = "guiaDTO.fechaEntrega", target = "fechaEmision", qualifiedByName = "localDateToString")
    @Mapping(source = "items", target = "detailItems")
    CreateGuiaRemisionRemitenteMessage toCreateGuiaRemisionRemitenteMessageWithData(GuiaRemisionGeneradaDTO guiaDTO, Integer idCliente, Integer idMotivo, List<ItemGuiaRemisionRemitenteMessage> items);

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.toString() : "";
    }
}
