package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.SessionPesajeActivaWithDetailProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PesajePersistenceMapper {

    @Mapping(target = "id_detordeningresopeso", ignore = true)
    @Mapping(target = "idOrdenIngreso", source = "idOrdenIngreso")
    @Mapping(target = "peso_rollo", ignore = true)
    @Mapping(target = "cod_rollo", ignore = true)
    @Mapping(target = "cnt_registrados", source = "cntRegistro")
    @Mapping(target = "completado", ignore = true)
    @Mapping(target = "cnt_rollos", source = "cntRollos")
    @Mapping(target = "id_detordeningreso", source = "idDetOrdenIngreso")
    @Mapping(target = "id_session_hidden", source = "id")
    PesajeDetalle toDomain(SessionPesajeActivaWithDetailProjection projection);
}
