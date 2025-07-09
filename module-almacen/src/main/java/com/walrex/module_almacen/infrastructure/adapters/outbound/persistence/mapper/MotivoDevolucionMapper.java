package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.MotivoDevolucionEntity;
import org.mapstruct.*;

/**
 * Mapper para convertir entre MotivoDevolucionEntity y MotivoDevolucionDTO
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, 
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MotivoDevolucionMapper {

    /**
     * Convierte Entity a DTO
     */
    MotivoDevolucionDTO toDTO(MotivoDevolucionEntity entity);

    /**
     * Convierte DTO a Entity
     */
    MotivoDevolucionEntity toEntity(MotivoDevolucionDTO dto);

    /**
     * Mapeo específico para creación (sin ID)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    MotivoDevolucionEntity toEntityForCreate(MotivoDevolucionDTO dto);

    /**
     * Mapeo específico para actualización (conserva timestamps)
     */
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    MotivoDevolucionEntity toEntityForUpdate(MotivoDevolucionDTO dto);
} 