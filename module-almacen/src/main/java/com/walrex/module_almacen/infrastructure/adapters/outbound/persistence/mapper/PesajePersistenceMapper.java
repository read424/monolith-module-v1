package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.SessionPesajeActivaWithDetailProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PesajePersistenceMapper {

    @Mapping(target = "id_detordeningresopeso", ignore = true)
    @Mapping(target = "id_ordeningreso", source = "id_ordeningreso")
    @Mapping(target = "peso_rollo", ignore = true)
    @Mapping(target = "cod_rollo", expression = "java(projection.getLote() + \"-\" + (projection.getCnt_registro() + 1))")
    @Mapping(target = "cnt_registrados", source = "cnt_registro")
    @Mapping(target = "completado", ignore = true)
    @Mapping(target = "id_detordeningreso", source = "id_detordeningreso")
    @Mapping(target = "id_session_hidden", source = "id")
    PesajeDetalle toDomain(SessionPesajeActivaWithDetailProjection projection);
}
