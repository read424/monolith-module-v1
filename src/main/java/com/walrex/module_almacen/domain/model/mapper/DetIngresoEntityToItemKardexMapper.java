package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetIngresoEntityToItemKardexMapper {
    @Mapping(source="id_articulo", target = "idArticulo")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "costo_compra", target = "valorUnidad")
    @Mapping(source = "id_ordeningreso", target = "idDocumento")
    @Mapping(source = "id", target = "idDetalleDocumento")
    ItemKardexDTO toItemKardex (DetailsIngresoEntity entity);
}
