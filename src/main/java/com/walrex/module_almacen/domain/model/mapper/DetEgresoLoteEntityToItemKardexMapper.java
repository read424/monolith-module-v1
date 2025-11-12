package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaLoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetEgresoLoteEntityToItemKardexMapper {

    @Mapping(source = "id_ordensalida", target = "idDocumento")
    @Mapping(source = "id_detalle_orden", target = "idDetalleDocumento")
    @Mapping(source = "monto_consumo", target = "valorUnidad")
    @Mapping(source = "total_monto", target = "valorTotal")
    ItemKardexDTO toItemKardex(DetailSalidaLoteEntity entity);
}
