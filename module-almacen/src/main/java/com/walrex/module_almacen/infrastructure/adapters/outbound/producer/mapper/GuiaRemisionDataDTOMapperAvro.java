package com.walrex.module_almacen.infrastructure.adapters.outbound.producer.mapper;

import org.mapstruct.*;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_almacen.domain.model.dto.DetailItemGuiaRemisionDTO;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDataDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionDataDTOMapperAvro {

    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "idMotivo", target = "idMotivo")
    @Mapping(source = "fechaEmision", target = "fechaEmision")
    @Mapping(source = "detailItems", target = "detailItems")
    CreateGuiaRemisionRemitenteMessage toAvro(GuiaRemisionGeneradaDataDTO guiaRemisionGenerada);

    @Mapping(source = "idProducto", target = "idProducto")
    @Mapping(source = "idOrdenSalida", target = "idOrdensalida")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "precio", target = "precio")
    @Mapping(source = "total", target = "subtotal")
    @Mapping(source = "idDetalleOrden", target = "idDetalleOrden")
    @Mapping(source = "peso", target = "peso")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "idTipoServicio", target = "tipoServicio")
    ItemGuiaRemisionRemitenteMessage toAvroItem(DetailItemGuiaRemisionDTO detailItem);
}
