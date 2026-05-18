package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.GamaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GamaPersistenceMapper {

    @Mapping(source = "id", target = "idGama")
    @Mapping(source = "name", target = "noGama")
    @Mapping(source = "order", target = "order")
    GamaEntity toEntity(Gama domain);

    @Mapping(source = "idGama", target = "id")
    @Mapping(source = "noGama", target = "name")
    @Mapping(source = "order", target = "order")
    Gama toDomain(GamaEntity entity);
}
