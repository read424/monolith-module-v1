package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KardexDetalleMapper {
    @Mapping(source = "idDocumento", target = "idOrdenDocumento")
    @Mapping(source = "tipoKardex", target = "typeKardex")
    @Mapping(source = "codigoDocumento", target = "descripcion")
    @Mapping(source = "valorUnidad", target = "precioCompra")
    @Mapping(source = "valorTotal", target = "totalCompra")
    @Mapping(source = "saldoStock", target = "stockActual")
    @Mapping(source = "saldoLote", target = "stockLote")
    @Mapping(source = "fechaMovimiento", target = "fecMovimiento")
    KardexDetalleDTO toDTO(KardexDetalleEnriquecido enriquecido);
}
