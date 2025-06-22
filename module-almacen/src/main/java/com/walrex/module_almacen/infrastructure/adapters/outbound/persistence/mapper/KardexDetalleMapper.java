package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KardexDetalleMapper {
    @Mapping(source = "idDocumento", target = "id_orden_documento")
    @Mapping(source = "detalle", target = "descripcion")
    @Mapping(source = "descDocumentoIngreso", target = "detail_document_ingreso")
    @Mapping(source = "tipoKardex", target = "type_kardex")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "descUnidad", target = "desc_unidad")
    @Mapping(source = "idUnidadSalida", target = "id_unidad_salida")
    @Mapping(source = "descUnidadSalida", target = "desc_unidad_salida")
    @Mapping(source = "valorUnidad", target = "precio_compra")
    @Mapping(source = "valorTotal", target = "total_compra")
    @Mapping(source = "saldoStock", target = "stock_actual")
    @Mapping(source = "saldoLote", target = "stock_lote")
    @Mapping(source = "fechaMovimiento", target = "fec_movimiento")
    KardexDetalleDTO toDTO(KardexDetalleEnriquecido enriquecido);
}
