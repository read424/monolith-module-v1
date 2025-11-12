package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.LoteDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaLoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LotePersistenceMapper {

    @Mapping(source = "id_salida_lote", target = "idsalida_ote")
    @Mapping(source = "monto_consumo", target = "precioUnitario")
    @Mapping(source = "total_monto", target = "totalMonto")
    LoteDTO toDTO(DetailSalidaLoteEntity entity);
}
