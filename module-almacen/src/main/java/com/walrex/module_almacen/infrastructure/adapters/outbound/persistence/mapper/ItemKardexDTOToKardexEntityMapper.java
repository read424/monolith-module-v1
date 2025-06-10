package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemKardexDTOToKardexEntityMapper {

    @Mapping(source = "typeKardex", target = "tipo_movimiento")
    @Mapping(source = "descripcion", target = "detalle")
    @Mapping(source = "valorUnidad", target = "costo")
    @Mapping(source = "valorTotal", target = "valorTotal")
    @Mapping(source = "fechaMovimiento", target = "fecha_movimiento")
    @Mapping(source = "idArticulo", target = "id_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "idUnidadSalida", target = "id_unidad_salida")
    @Mapping(source = "idAlmacen", target = "id_almacen")
    @Mapping(source = "idDocumento", target = "id_documento")
    @Mapping(source = "idLote", target = "id_lote")
    @Mapping(source = "idDetalleDocumento", target = "id_detalle_documento")
    @Mapping(source = "saldoStock", target = "saldo_actual")
    @Mapping(source = "saldoLote", target = "saldoLote")
    KardexEntity toEntity(ItemKardexDTO itemKardex);
}
