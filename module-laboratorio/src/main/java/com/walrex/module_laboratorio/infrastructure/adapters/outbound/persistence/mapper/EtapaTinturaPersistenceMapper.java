package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.EtapaTinturaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EtapaTinturaPersistenceMapper {
    EtapaTinturaEntity toEntity(EtapaTintura domain);

    EtapaTintura toDomain(EtapaTinturaEntity entity);
}
